package com.searchmetrics.exchangerates.api.controllers;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.searchmetrics.exchangerates.api.validators.ConsistentDateRange;

@RestController
@RequestMapping("/api/rates")
@Validated
public class RatesController {
	
	@GetMapping
	public void getRates() {
		System.out.println("RatesController.getRates()");
	}


	@GetMapping(value = "/snapshots")
	@ConsistentDateRange
	public void getRates(
			@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		System.out.println("RatesController.getRates()");
	}

}
