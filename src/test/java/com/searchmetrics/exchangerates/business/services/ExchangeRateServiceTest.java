package com.searchmetrics.exchangerates.business.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.searchmetrics.exchangerates.business.dtos.RateResponse;
import com.searchmetrics.exchangerates.business.exceptions.BusinessException;
import com.searchmetrics.exchangerates.business.mappers.RateResponseMapper;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate_;
import com.searchmetrics.exchangerates.persistence.repositories.ExchangeRateRepository;
import com.searchmetrics.exchangerates.providers.bitpay.BitPayExchangeRateProvider;
import com.searchmetrics.exchangerates.providers.exceptions.ProviderException;

@ExtendWith(SpringExtension.class)
public class ExchangeRateServiceTest {

	@MockBean
	private BitPayExchangeRateProvider provider;

	@MockBean
	private ExchangeRateRepository repo;

	private ExchangeRateService service;
	
	private RateResponseMapper mapper = new RateResponseMapper();

	@BeforeEach
	void setup() {
		service = new ExchangeRateService(repo, provider, mapper);
	}
	
	@Test
	void latestRateFromProvider() {
		
		when(provider.conversionRate("BTC", "USD")).thenReturn(Optional.of(BigDecimal.valueOf(0.01)));
		when(provider.name()).thenReturn("test");
		
		RateResponse result = service.latestRate();
		
		assertThat(result).isNotNull();
		assertThat(result.getExchangeRate()).isEqualTo(BigDecimal.valueOf(0.01));
		assertThat(result.getFrom()).isEqualTo("BTC");
		assertThat(result.getTo()).isEqualTo("USD");
		assertThat(result.getLastUpdated()).isNotNull();
		assertThat(result.getProvider()).isEqualTo("test");
		
		verify(provider, times(1)).conversionRate("BTC", "USD");
		verify(provider, times(1)).name();
		verifyNoInteractions(repo);
	}
	
	@Test
	void latestRateProviderNoResultDBFallback() {
		
		when(provider.conversionRate("BTC", "USD")).thenReturn(Optional.empty());
		when(repo.findTopByOrderByCreatedAtDesc())
			.thenReturn(Optional.of(ExchangeRate.builder().id(1L).fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.01))
					.providerName("test").createdAt(LocalDate.of(2020, 9, 1).atStartOfDay().toInstant(ZoneOffset.UTC))
					.updatedAt(LocalDate.of(2020, 9, 2).atStartOfDay().toInstant(ZoneOffset.UTC)).build()));
		
		RateResponse result = service.latestRate();
		
		assertThat(result).isNotNull();
		assertThat(result.getExchangeRate()).isEqualTo(BigDecimal.valueOf(0.01));
		assertThat(result.getFrom()).isEqualTo("BTC");
		assertThat(result.getTo()).isEqualTo("USD");
		assertThat(result.getLastUpdated()).isEqualTo("2020-09-01T00:00:00Z");
		assertThat(result.getProvider()).isEqualTo("test");
		
		verify(provider, times(1)).conversionRate("BTC", "USD");
		verify(provider, times(0)).name();
		verify(repo, times(1)).findTopByOrderByCreatedAtDesc();
	}
	
	@Test
	void latestRateProviderErrorDBFallback() {
		
		when(provider.conversionRate("BTC", "USD")).thenThrow(ProviderException.class);
		when(repo.findTopByOrderByCreatedAtDesc())
			.thenReturn(Optional.of(ExchangeRate.builder().id(1L).fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.01))
					.providerName("test").createdAt(LocalDate.of(2020, 9, 1).atStartOfDay().toInstant(ZoneOffset.UTC))
					.updatedAt(LocalDate.of(2020, 9, 2).atStartOfDay().toInstant(ZoneOffset.UTC)).build()));
		
		RateResponse result = service.latestRate();
		
		assertThat(result).isNotNull();
		assertThat(result.getExchangeRate()).isEqualTo(BigDecimal.valueOf(0.01));
		assertThat(result.getFrom()).isEqualTo("BTC");
		assertThat(result.getTo()).isEqualTo("USD");
		assertThat(result.getLastUpdated()).isEqualTo("2020-09-01T00:00:00Z");
		assertThat(result.getProvider()).isEqualTo("test");
		
		verify(provider, times(1)).conversionRate("BTC", "USD");
		verify(provider, times(0)).name();
		verify(repo, times(1)).findTopByOrderByCreatedAtDesc();
	}
	
	@Test
	void latestRateProviderErrorDBFallbackNoResult() {
		
		when(provider.conversionRate("BTC", "USD")).thenThrow(ProviderException.class);
		when(repo.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());
		
		BusinessException exception = assertThrows(BusinessException.class, () -> service.latestRate());
		
		assertThat(exception.getReason()).isEqualTo("Conversion rate for provided currencies not found.");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		
		verify(provider, times(1)).conversionRate("BTC", "USD");
		verify(provider, times(0)).name();
		verify(repo, times(1)).findTopByOrderByCreatedAtDesc();
	}
	
	@Test
	void latestRateProviderNoResultDBFallbackNoResult() {
		
		when(provider.conversionRate("BTC", "USD")).thenReturn(Optional.empty());
		when(repo.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());
		
		BusinessException exception = assertThrows(BusinessException.class, () -> service.latestRate());
		
		assertThat(exception.getReason()).isEqualTo("Conversion rate for provided currencies not found.");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		
		verify(provider, times(1)).conversionRate("BTC", "USD");
		verify(provider, times(0)).name();
		verify(repo, times(1)).findTopByOrderByCreatedAtDesc();
	}
	
	@Test
	void rateSnapshots() {
		
		List<RateResponse> expected = List.of(
				RateResponse.builder()
				.provider("test").from("BTC").to("USD").exchangeRate(BigDecimal.valueOf(0.01))
				.lastUpdated(LocalDate.of(2020, 9, 3).atStartOfDay().toInstant(ZoneOffset.UTC)).build(),
				RateResponse.builder()
				.provider("test").from("BTC").to("USD").exchangeRate(BigDecimal.valueOf(0.02))
				.lastUpdated(LocalDate.of(2020, 9, 2).atStartOfDay().toInstant(ZoneOffset.UTC)).build());
		
		when(repo.findAll(ArgumentMatchers.<Specification<ExchangeRate>>any(), eq(Sort.by(Direction.DESC, ExchangeRate_.CREATED_AT))))
		.thenReturn(
				List.of(
					ExchangeRate.builder().id(1L).fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.01))
					.providerName("test").createdAt(LocalDate.of(2020, 9, 3).atStartOfDay().toInstant(ZoneOffset.UTC))
					.updatedAt(LocalDate.of(2020, 9, 3).atStartOfDay().toInstant(ZoneOffset.UTC)).build(),
					ExchangeRate.builder().id(2L).fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.02))
					.providerName("test").createdAt(LocalDate.of(2020, 9, 2).atStartOfDay().toInstant(ZoneOffset.UTC))
					.updatedAt(LocalDate.of(2020, 9, 2).atStartOfDay().toInstant(ZoneOffset.UTC)).build()
				));
		
		
		List<RateResponse> results = service.rateSnapshots(LocalDateTime.of(2020, 9, 1, 10, 0), null);
		
		assertThat(results).isNotNull();
		assertThat(results).hasSize(2);
		assertThat(results.get(0)).isEqualTo(expected.get(0));
		assertThat(results.get(1)).isEqualTo(expected.get(1));
		
		verifyNoInteractions(provider);
	}
	
}
