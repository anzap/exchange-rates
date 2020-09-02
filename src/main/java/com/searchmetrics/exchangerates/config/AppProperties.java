package com.searchmetrics.exchangerates.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class AppProperties {

	private final Providers providers;

	@Getter
	@RequiredArgsConstructor
	public static class Providers {
		private final ProviderConfig blockchain;
	}

	@Getter
	@RequiredArgsConstructor
	public static class ProviderConfig {
		private final String baseurl;
		private final Long timeout;
	}
}