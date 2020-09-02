package com.searchmetrics.exchangerates.business.providers.blockchain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.searchmetrics.exchangerates.business.exceptions.BusinessException;
import com.searchmetrics.exchangerates.business.providers.bitpay.BitPayExchangeRateProvider;
import com.searchmetrics.exchangerates.config.AppProperties;
import com.searchmetrics.exchangerates.config.AppProperties.ProviderConfig;
import com.searchmetrics.exchangerates.config.AppProperties.Providers;

import io.netty.handler.timeout.ReadTimeoutException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

@ExtendWith(SpringExtension.class)
public class BitPayExchangeRateProviderTest {

	private BitPayExchangeRateProvider provider;

	public static MockWebServer mockBackEnd;

	@BeforeAll
	static void setUp() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockBackEnd.shutdown();
	}

	@BeforeEach
	void initialize() {
		String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
		provider = new BitPayExchangeRateProvider(new AppProperties(new Providers(new ProviderConfig(baseUrl, 500L, ""))));
	}

	@Test
	void validProviderExhangeRate() {

		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\",\"rate\":11513.27}}")
						.addHeader("Content-Type", "application/json"));

		Optional<BigDecimal> conversionRate = provider.conversionRate("BTC", "USD");

		assertThat(conversionRate).isNotEmpty();
		assertThat(conversionRate.get()).isEqualTo(BigDecimal.valueOf(11513.270));
	}

	@Test
	void noProviderExhangeRate() {

		mockBackEnd.enqueue(new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\"}}")
				.addHeader("Content-Type", "application/json"));

		Optional<BigDecimal> conversionRate = provider.conversionRate("BTC", "USD");

		assertThat(conversionRate).isEmpty();
	}

	@Test
	void invalidCurrency() {

		mockBackEnd.enqueue(new MockResponse().setBody("{\"error\":\"Invalid base currency specified\"}")
				.addHeader("Content-Type", "application/json"));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> provider.conversionRate("BTX", "USD"));

		assertThat(exception.getReason()).isEqualTo("Invalid base currency specified");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	void emptyProviderResponse() {

		mockBackEnd.enqueue(new MockResponse().addHeader("Content-Type", "application/json"));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> provider.conversionRate("BTX", "USD"));

		assertThat(exception.getReason()).isEqualTo("Unexpected response from bitpay provider received!");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	void incompatibleProviderResponse() {

		mockBackEnd.enqueue(new MockResponse().setBody("{\"code\":\"USD\",\"name\":\"US Dollar\",\"rate\":11513.27}")
				.addHeader("Content-Type", "application/json"));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> provider.conversionRate("BTX", "USD"));

		assertThat(exception.getReason()).isEqualTo("Unexpected response from bitpay provider received!");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	void providerError400() {

		mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()).addHeader("Content-Type",
				"application/json"));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> provider.conversionRate("BTX", "USD"));

		assertThat(exception.getReason()).isEqualTo("No exchange rate could be calculated for provided currencies!");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void providerError500() {

		mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.addHeader("Content-Type", "application/json"));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> provider.conversionRate("BTX", "USD"));

		assertThat(exception.getReason()).isEqualTo("Rate provider connection failing!");
		assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	void providerTimeoutError() {

		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\",\"rate\":11513.27}}")
						.addHeader("Content-Type", "application/json").setSocketPolicy(SocketPolicy.NO_RESPONSE));

		assertThrows(ReadTimeoutException.class, () -> provider.conversionRate("BTC", "USD"));

	}

}
