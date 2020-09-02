package com.searchmetrics.exchangerates.business.services;

import java.math.BigDecimal;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.searchmetrics.exchangerates.business.providers.CurrencyExchangeRateProvider;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate;
import com.searchmetrics.exchangerates.persistence.repositories.ExchangeRateRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ExchangeRateCollector {
	
	private final CurrencyExchangeRateProvider provider;
	private final ExchangeRateRepository repo;
	
	@Scheduled(cron = "${app.providers.bitpay.refreshRate}")
	@Transactional
	public void collect() {
		log.info("Calling {} provider to get latest exchange rate info", provider.name());
		
		Optional<BigDecimal> conversionRate = provider.conversionRate("BTC", "USD");
		
		conversionRate.ifPresentOrElse(c -> {
			repo.save(ExchangeRate.builder().providerName(provider.name()).fromCurrency("BTC").toCurrency("USD").exchangeRate(conversionRate.get()).build());
		}, () -> log.error("Error getting conversion rate from {} provider", provider.name()));
		
	}

}
