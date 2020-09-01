package com.searchmetrics.exchangerates.persistence.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.searchmetrics.exchangerates.peristence.models.ExchangeRate;
import com.searchmetrics.exchangerates.peristence.repositories.ExchangeRateRepository;
import com.searchmetrics.exchangerates.peristence.repositories.ExchangeRateSpecifications;

@DataJpaTest
public class ExchangeRateRepositoryTest {

	@Autowired
	private ExchangeRateRepository repo;

	@Test
	void save() {

		ExchangeRate saved = repo.save(ExchangeRate.builder().fromCurrency("BTC").toCurrency("USD")
				.exchangeRate(BigDecimal.valueOf(0.000078)).build());

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
		assertThat(saved.getFromCurrency()).isEqualTo("BTC");
		assertThat(saved.getToCurrency()).isEqualTo("USD");
		assertThat(saved.getExchangeRate()).isEqualTo(BigDecimal.valueOf(0.000078));

	}

	@Test
	void dateRangeQuery() {

		ExchangeRate first = repo.save(
				ExchangeRate.builder().fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.000078))
						.createdAt(LocalDateTime.of(2020, 8, 5, 12, 00).atZone(ZoneOffset.UTC).toInstant()).build());

		ExchangeRate second = repo.save(
				ExchangeRate.builder().fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.000078))
						.createdAt(LocalDateTime.of(2020, 9, 5, 12, 00).atZone(ZoneOffset.UTC).toInstant()).build());

		ExchangeRate third = repo.save(
				ExchangeRate.builder().fromCurrency("BTC").toCurrency("USD").exchangeRate(BigDecimal.valueOf(0.000078))
						.createdAt(LocalDateTime.of(2020, 10, 5, 12, 00).atZone(ZoneOffset.UTC).toInstant()).build());

		List<ExchangeRate> results = repo.findAll(ExchangeRateSpecifications
				.after(LocalDateTime.of(2020, 9, 1, 12, 00).atZone(ZoneOffset.UTC).toInstant()));
		
		assertThat(results).hasSize(2);
		assertThat(results.get(0)).isEqualTo(second);
		assertThat(results.get(1)).isEqualTo(third);
		
		results = repo.findAll(ExchangeRateSpecifications
				.before(LocalDateTime.of(2020, 9, 1, 12, 00).atZone(ZoneOffset.UTC).toInstant()));
		
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).isEqualTo(first);
		
		results = repo.findAll(ExchangeRateSpecifications
				.after(LocalDateTime.of(2020, 9, 1, 12, 00).atZone(ZoneOffset.UTC).toInstant())
				.and(ExchangeRateSpecifications
				.before(LocalDateTime.of(2020, 10, 1, 12, 00).atZone(ZoneOffset.UTC).toInstant())));
		
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).isEqualTo(second);

	}

}
