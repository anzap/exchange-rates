package com.searchmetrics.exchangerates.providers;

import java.math.BigDecimal;
import java.util.Optional;

public interface CurrencyExchangeRateProvider {

	Optional<BigDecimal> conversionRate(String from, String to);
	
	String name();
}
