package com.searchmetrics.exchangerates.providers.bitpay;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
class ApiResponse {

	private Data data;
	private String error;
	
	public Optional<Data> getData() {
		return Optional.ofNullable(data);
	}
	
	public Optional<String> getError() {
		return Optional.ofNullable(error);
	}

	@NoArgsConstructor
	@Getter
	@Setter
	static class Data {
		private String code;
		private String name;
		private BigDecimal rate;
	}

}
