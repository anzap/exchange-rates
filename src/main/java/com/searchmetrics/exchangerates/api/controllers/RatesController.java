package com.searchmetrics.exchangerates.api.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.searchmetrics.exchangerates.api.validators.ConsistentDateRange;
import com.searchmetrics.exchangerates.business.dtos.RateResponse;
import com.searchmetrics.exchangerates.business.services.ExchangeRateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/rates")
@Validated
@Slf4j
@AllArgsConstructor
public class RatesController {
	
	private final ExchangeRateService service;
	
	@Operation(summary = "Retrieves latest exchange rate between provided in and out currencies.")
	@GetMapping
	public RateResponse latestRate(
			@Parameter(description = "Currency code to convert from (default BTC)", required = false)
			@RequestParam(value = "in", defaultValue = "BTC") String in,
			
			@Parameter(description = "Currency code to convert to (default USD)", required = false)
            @RequestParam(value = "out", defaultValue = "USD") String out) {
		log.info("Getting latest rate");
		return service.latestRate(in, out);
	}


	@Operation(summary = "Retrieves historical exchange rate information between provided in and out currencies. Results limited in period from and to. ")
	@GetMapping(value = "/snapshots")
	@ConsistentDateRange
	public List<RateResponse> rateSnapshots(
			@Parameter(description = "Currency code to convert from (default BTC)", required = false)
			@RequestParam(value = "in", defaultValue = "BTC") String in,
			
			@Parameter(description = "Currency code to convert to (default USD)", required = false)
			@RequestParam(value = "out", defaultValue = "USD") String out,
			
			@Parameter(description = "Time period start date to get historical data for", required = false)
			@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			
			@Parameter(description = "Time period end date to get historical data for", required = false)
			@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		log.info("Getting historic rates");
		return service.rateSnapshots(in, out, from, to);
	}

}
