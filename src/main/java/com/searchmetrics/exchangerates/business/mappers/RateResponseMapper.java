package com.searchmetrics.exchangerates.business.mappers;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.searchmetrics.exchangerates.business.dtos.RateResponse;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate;

@Component
public class RateResponseMapper implements Function<ExchangeRate, RateResponse> {

	@Override
	public RateResponse apply(ExchangeRate t) {
		return RateResponse.builder()
				.from(t.getFromCurrency())
				.to(t.getToCurrency())
				.exchangeRate(t.getExchangeRate())
				.provider(t.getProviderName())
				.lastUpdated(t.getCreatedAt())
				.build();
	}

}
