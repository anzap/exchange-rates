package com.searchmetrics.exchangerates.business.dtos;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RateResponse {
	
	private String from;
	private String to;
	private BigDecimal exchangeRate;
	private String provider;
	private Instant lastUpdated;

}
