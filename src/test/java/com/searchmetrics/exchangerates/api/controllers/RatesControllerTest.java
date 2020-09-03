package com.searchmetrics.exchangerates.api.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import com.searchmetrics.exchangerates.business.dtos.RateResponse;
import com.searchmetrics.exchangerates.business.exceptions.BusinessException;
import com.searchmetrics.exchangerates.business.services.ExchangeRateService;

@WebMvcTest(RatesController.class)
public class RatesControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private ExchangeRateService service;

	@Test
	void latestRate() throws Exception {
		
		when(service.latestRate("BTC", "USD")).thenReturn(
				RateResponse.builder()
				.from("BTC")
				.to("USD")
				.exchangeRate(BigDecimal.valueOf(0.01))
				.provider("test")
				.lastUpdated(LocalDate.of(2020, 9, 1).atStartOfDay().toInstant(ZoneOffset.UTC))
				.build());

		mvc.perform(get("/api/rates"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.from", is("BTC")))
			.andExpect(jsonPath("$.to", is("USD")))
			.andExpect(jsonPath("$.exchangeRate", is(0.01)))
			.andExpect(jsonPath("$.provider", is("test")))
			.andExpect(jsonPath("$.lastUpdated", is("2020-09-01T00:00:00Z")));
		
		verify(service, times(1)).latestRate("BTC", "USD");

	}
	
	@Test
	void latestRateNotFoundError() throws Exception {
		
		when(service.latestRate("BTC", "USD")).thenThrow(new BusinessException(HttpStatus.NOT_FOUND,
						"Conversion rate for provided currencies not found."));

		mvc.perform(get("/api/rates"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status", is("NOT_FOUND")))
			.andExpect(jsonPath("$.time", is(notNullValue())))
			.andExpect(jsonPath("$.path", is("/api/rates")))
			.andExpect(jsonPath("$.method", is("GET")))
			.andExpect(jsonPath("$.errors[0].message", is("Conversion rate for provided currencies not found.")));
		
		verify(service, times(1)).latestRate("BTC", "USD");

	}

	@Test
	void rateSnapshotsFrom() throws Exception {
		
		when(service.rateSnapshots("BTC", "USD", LocalDateTime.of(2020, 8, 1, 10, 0), null)).thenReturn(
				List.of(
						RateResponse.builder()
						.provider("test").from("BTC").to("USD").exchangeRate(BigDecimal.valueOf(0.01))
						.lastUpdated(LocalDate.of(2020, 8, 2).atStartOfDay().toInstant(ZoneOffset.UTC)).build(),
						RateResponse.builder()
						.provider("test").from("BTC").to("USD").exchangeRate(BigDecimal.valueOf(0.02))
						.lastUpdated(LocalDate.of(2020, 8, 3).atStartOfDay().toInstant(ZoneOffset.UTC)).build()
				));

		mvc.perform(get("/api/rates/snapshots").param("from", "2020-08-01T10:00:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].from", is("BTC")))
			.andExpect(jsonPath("$[0].to", is("USD")))
			.andExpect(jsonPath("$[0].exchangeRate", is(0.01)))
			.andExpect(jsonPath("$[0].provider", is("test")))
			.andExpect(jsonPath("$[0].lastUpdated", is("2020-08-02T00:00:00Z")))
			.andExpect(jsonPath("$[1].from", is("BTC")))
			.andExpect(jsonPath("$[1].to", is("USD")))
			.andExpect(jsonPath("$[1].exchangeRate", is(0.02)))
			.andExpect(jsonPath("$[1].provider", is("test")))
			.andExpect(jsonPath("$[1].lastUpdated", is("2020-08-03T00:00:00Z")));
		
		verify(service, times(1)).rateSnapshots("BTC", "USD", LocalDateTime.of(2020, 8, 1, 10, 0), null);

	}
	
	@Test
	void rateSnapshotsBetween() throws Exception {
		
		when(service.rateSnapshots("BTC", "USD", LocalDateTime.of(2020, 7, 1, 9, 0), LocalDateTime.of(2020, 8, 1, 10, 0))).thenReturn(
				List.of(
						RateResponse.builder()
						.provider("test").from("BTC").to("USD").exchangeRate(BigDecimal.valueOf(0.01))
						.lastUpdated(LocalDate.of(2020, 8, 2).atStartOfDay().toInstant(ZoneOffset.UTC)).build(),
						RateResponse.builder()
						.provider("test").from("BTC").to("USD").exchangeRate(BigDecimal.valueOf(0.02))
						.lastUpdated(LocalDate.of(2020, 8, 3).atStartOfDay().toInstant(ZoneOffset.UTC)).build()
				));

		mvc.perform(get("/api/rates/snapshots").param("from", "2020-07-01T09:00").param("to", "2020-08-01T10:00"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
	            .andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].from", is("BTC")))
				.andExpect(jsonPath("$[0].to", is("USD")))
				.andExpect(jsonPath("$[0].exchangeRate", is(0.01)))
				.andExpect(jsonPath("$[0].provider", is("test")))
				.andExpect(jsonPath("$[0].lastUpdated", is("2020-08-02T00:00:00Z")))
				.andExpect(jsonPath("$[1].from", is("BTC")))
				.andExpect(jsonPath("$[1].to", is("USD")))
				.andExpect(jsonPath("$[1].exchangeRate", is(0.02)))
				.andExpect(jsonPath("$[1].provider", is("test")))
				.andExpect(jsonPath("$[1].lastUpdated", is("2020-08-03T00:00:00Z")));
		
		verify(service, times(1)).rateSnapshots("BTC", "USD", LocalDateTime.of(2020, 7, 1, 9, 0), LocalDateTime.of(2020, 8, 1, 10, 0));

	}

	@Test
	void invalidRequestDateParams() throws Exception {
		mvc.perform(get("/api/rates/snapshots"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status", is("BAD_REQUEST")))
			.andExpect(jsonPath("$.time", is(notNullValue())))
			.andExpect(jsonPath("$.path", is("/api/rates/snapshots")))
			.andExpect(jsonPath("$.method", is("GET")))
			.andExpect(jsonPath("$.errors[0].message", is("Date range passed in request is invalid")));

		mvc.perform(get("/api/rates/snapshots").param("to", "2020-08-01T10:00:00"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status", is("BAD_REQUEST")))
			.andExpect(jsonPath("$.time", is(notNullValue())))
			.andExpect(jsonPath("$.path", is("/api/rates/snapshots")))
			.andExpect(jsonPath("$.method", is("GET")))
			.andExpect(jsonPath("$.errors[0].message", is("Date range passed in request is invalid")));

		mvc.perform(get("/api/rates/snapshots").param("from", "2020-09-01T10:00:00").param("to", "2020-08-01T10:00:00"))
			.andExpect(jsonPath("$.status", is("BAD_REQUEST")))
			.andExpect(jsonPath("$.time", is(notNullValue())))
			.andExpect(jsonPath("$.path", is("/api/rates/snapshots")))
			.andExpect(jsonPath("$.method", is("GET")))
			.andExpect(jsonPath("$.errors[0].message", is("Date range passed in request is invalid")));
		
		verifyNoInteractions(service);

	}

}
