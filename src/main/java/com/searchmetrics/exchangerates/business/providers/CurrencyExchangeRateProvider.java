package com.searchmetrics.exchangerates.business.providers;

import java.math.BigDecimal;
import java.util.Optional;

public interface CurrencyExchangeRateProvider {

	Optional<BigDecimal> conversionRate(String from, String to);
}
