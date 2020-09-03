package com.searchmetrics.exchangerates.business.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.searchmetrics.exchangerates.business.dtos.RateResponse;
import com.searchmetrics.exchangerates.business.exceptions.BusinessException;
import com.searchmetrics.exchangerates.business.mappers.RateResponseMapper;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate_;
import com.searchmetrics.exchangerates.persistence.repositories.ExchangeRateRepository;
import com.searchmetrics.exchangerates.persistence.repositories.ExchangeRateSpecifications;
import com.searchmetrics.exchangerates.providers.CurrencyExchangeRateProvider;
import com.searchmetrics.exchangerates.providers.exceptions.ProviderException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ExchangeRateService {

	private final ExchangeRateRepository repo;
	private final CurrencyExchangeRateProvider provider;
	private final RateResponseMapper mapper;

	public RateResponse latestRate(String in, String out) {
		
		log.info("Calculating latest exchange rate between {} and {}", in, out);

		try {
			Optional<BigDecimal> conversionRate = provider.conversionRate(in, out);

			if (conversionRate.isEmpty()) {
				log.warn("Provider did not return exchange rate, falling back to database.");
				return latestFromDB(in, out);
			}

			return RateResponse.builder().from(in).to(out).provider(provider.name()).lastUpdated(Instant.now())
					.exchangeRate(conversionRate.get()).build();

		} catch (ProviderException e) {
			log.error("Error getting exchange rate from provider, falling back to database.", e);
			return latestFromDB(in, out);
		}

	}

	public List<RateResponse> rateSnapshots(String in, String out, LocalDateTime from, LocalDateTime to) {

		Specification<ExchangeRate> spec = Specification.where(null);
		
		if (in != null) {
			spec = spec.and(ExchangeRateSpecifications.fromCurrency(in));
		}
		
		if (out != null) {
			spec = spec.and(ExchangeRateSpecifications.toCurrency(out));
		}

		if (from != null) {
			spec = spec.and(ExchangeRateSpecifications.after(from.atZone(ZoneOffset.UTC).toInstant()));
		}

		if (to != null) {
			spec = spec.and(ExchangeRateSpecifications.before(to.atZone(ZoneOffset.UTC).toInstant()));
		}

		return repo.findAll(spec, Sort.by(Direction.DESC, ExchangeRate_.CREATED_AT)).stream().map(mapper)
				.collect(Collectors.toList());
	}

	private RateResponse latestFromDB(String in, String out) {
		return repo.findTopByFromCurrencyAndToCurrencyOrderByCreatedAtDesc(in, out).map(mapper)
				.orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
						"Conversion rate for provided currencies not found."));
	}

}
