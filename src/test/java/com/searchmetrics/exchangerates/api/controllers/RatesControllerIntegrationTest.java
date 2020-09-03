package com.searchmetrics.exchangerates.api.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RatesControllerIntegrationTest {

	@Autowired
	private WebTestClient client;

	public static MockWebServer mockBackEnd;

	@BeforeAll
	static void setup() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start(9999);
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockBackEnd.shutdown();
	}

	@Test
	public void latestRateFromProvider() {
		
		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\",\"rate\":11513.27}}")
						.addHeader("Content-Type", "application/json"));

		this.client.get().uri("/api/rates")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.from").isEqualTo("BTC")
			.jsonPath("$.to").isEqualTo("USD")
			.jsonPath("$.exchangeRate").isEqualTo(11513.27)
			.jsonPath("$.provider").isEqualTo("BitPay")
			.jsonPath("$.lastUpdated").isNotEmpty();

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql", "classpath:fixtures/exchange-rates.sql"})
	public void latestRateProviderNoRateDbFallback() {
		
		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\"}}")
						.addHeader("Content-Type", "application/json"));

		this.client.get().uri("/api/rates")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.from").isEqualTo("BTC")
			.jsonPath("$.to").isEqualTo("USD")
			.jsonPath("$.exchangeRate").isEqualTo(11000.01)
			.jsonPath("$.provider").isEqualTo("BitPay")
			.jsonPath("$.lastUpdated").isNotEmpty();

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql", "classpath:fixtures/exchange-rates.sql"})
	public void latestRateProviderErrorDbFallback() {
		
		mockBackEnd.enqueue(new MockResponse().setBody("{\"error\":\"Invalid base currency specified\"}")
				.addHeader("Content-Type", "application/json"));

		this.client.get().uri("/api/rates")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.from").isEqualTo("BTC")
			.jsonPath("$.to").isEqualTo("USD")
			.jsonPath("$.exchangeRate").isEqualTo(11000.01)
			.jsonPath("$.provider").isEqualTo("BitPay")
			.jsonPath("$.lastUpdated").isNotEmpty();

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql", "classpath:fixtures/exchange-rates.sql"})
	public void latestRateProviderTimeoutDbFallback() {
		
		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\",\"rate\":11513.27}}")
						.addHeader("Content-Type", "application/json").setSocketPolicy(SocketPolicy.NO_RESPONSE));

		this.client.get().uri("/api/rates")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.from").isEqualTo("BTC")
			.jsonPath("$.to").isEqualTo("USD")
			.jsonPath("$.exchangeRate").isEqualTo(11000.01)
			.jsonPath("$.provider").isEqualTo("BitPay")
			.jsonPath("$.lastUpdated").isNotEmpty();

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql"})
	public void latestRateProviderNoRateDbNotFound() {
		
		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"USD\",\"name\":\"US Dollar\"}}")
						.addHeader("Content-Type", "application/json"));

		this.client.get().uri("/api/rates")
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.status").isEqualTo("NOT_FOUND")
			.jsonPath("$.time").isNotEmpty()
			.jsonPath("$.path").isEqualTo("/api/rates")
			.jsonPath("$.method").isEqualTo("GET")
			.jsonPath("$.errors[0].message").isEqualTo("Conversion rate for provided currencies not found.");

	}
	
	@Test
	public void latestRateFromProviderNoDefaultCurrencies() {
		
		mockBackEnd.enqueue(
				new MockResponse().setBody("{\"data\": {\"code\":\"EUR\",\"name\":\"Eurozone Euro\",\"rate\":340.27}}")
						.addHeader("Content-Type", "application/json"));

		this.client.get().uri("/api/rates?in=ETH&out=EUR")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.from").isEqualTo("ETH")
			.jsonPath("$.to").isEqualTo("EUR")
			.jsonPath("$.exchangeRate").isEqualTo(340.27)
			.jsonPath("$.provider").isEqualTo("BitPay")
			.jsonPath("$.lastUpdated").isNotEmpty();

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql", "classpath:fixtures/exchange-rates.sql"})
	public void rateSnapshots() {
		
		this.client.get().uri("/api/rates/snapshots?from=2020-09-01T00:00:00Z&to=2020-09-01T09:45:00Z")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].from").isEqualTo("BTC")
			.jsonPath("$[0].to").isEqualTo("USD")
			.jsonPath("$[0].exchangeRate").isEqualTo(11300.21)
			.jsonPath("$[0].provider").isEqualTo("BitPay")
			.jsonPath("$[0].lastUpdated").isEqualTo("2020-09-01T09:40:00Z")
			.jsonPath("$[1].from").isEqualTo("BTC")
			.jsonPath("$[1].to").isEqualTo("USD")
			.jsonPath("$[1].exchangeRate").isEqualTo(11300.01)
			.jsonPath("$[1].provider").isEqualTo("BitPay")
			.jsonPath("$[1].lastUpdated").isEqualTo("2020-09-01T09:30:00Z");

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql", "classpath:fixtures/exchange-rates.sql"})
	public void rateSnapshotsFromDateOnly() {
		
		this.client.get().uri("/api/rates/snapshots?from=2020-09-01T09:45:00Z")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].from").isEqualTo("BTC")
			.jsonPath("$[0].to").isEqualTo("USD")
			.jsonPath("$[0].exchangeRate").isEqualTo(11000.01)
			.jsonPath("$[0].provider").isEqualTo("BitPay")
			.jsonPath("$[0].lastUpdated").isEqualTo("2020-09-01T10:00:00Z");

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql"})
	public void rateSnapshotsNoDbData() {
		
		this.client.get().uri("/api/rates/snapshots?from=2020-09-01T09:45:00Z")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(0);

	}
	
	@Test
	public void rateSnapshotsNoParams() {
		
		this.client.get().uri("/api/rates/snapshots")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.status").isEqualTo("BAD_REQUEST")
			.jsonPath("$.time").isNotEmpty()
			.jsonPath("$.path").isEqualTo("/api/rates/snapshots")
			.jsonPath("$.method").isEqualTo("GET")
			.jsonPath("$.errors[0].message").isEqualTo("Date range passed in request is invalid");

	}
	
	@Test
	public void rateSnapshotsToDateOnly() {
		
		this.client.get().uri("/api/rates/snapshots?to=2020-09-01T00:00:00Z")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.status").isEqualTo("BAD_REQUEST")
			.jsonPath("$.time").isNotEmpty()
			.jsonPath("$.path").isEqualTo("/api/rates/snapshots")
			.jsonPath("$.method").isEqualTo("GET")
			.jsonPath("$.errors[0].message").isEqualTo("Date range passed in request is invalid");

	}
	
	@Test
	@Sql({"classpath:fixtures/resetDB.sql", "classpath:fixtures/exchange-rates.sql"})
	public void rateSnapshotsAlternateCurrency() {
		
		this.client.get().uri("/api/rates/snapshots?from=2020-08-01T09:30:00Z&in=ETH&out=EUR")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].from").isEqualTo("ETH")
			.jsonPath("$[0].to").isEqualTo("EUR")
			.jsonPath("$[0].exchangeRate").isEqualTo(10300.01)
			.jsonPath("$[0].provider").isEqualTo("BitPay")
			.jsonPath("$[0].lastUpdated").isEqualTo("2020-08-01T09:30:00Z");

	}

}
