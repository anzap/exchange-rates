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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/rates")
@Validated
@Slf4j
@AllArgsConstructor
public class RatesController {
	
	private final ExchangeRateService service;
	
	@GetMapping
	public RateResponse latestRate(
			@RequestParam(value = "in", defaultValue = "BTC") String in,
            @RequestParam(value = "out", defaultValue = "USD") String out) {
		log.info("Getting latest rate");
		return service.latestRate(in, out);
	}


	@GetMapping(value = "/snapshots")
	@ConsistentDateRange
	public List<RateResponse> rateSnapshots(
			@RequestParam(value = "in", defaultValue = "BTC") String in,
            @RequestParam(value = "out", defaultValue = "USD") String out,
			@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		log.info("Getting historic rates");
		return service.rateSnapshots(in, out, from, to);
	}

}
