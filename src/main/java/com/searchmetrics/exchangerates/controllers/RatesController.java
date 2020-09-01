package com.searchmetrics.exchangerates.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.searchmetrics.exchangerates.controllers.validators.ConsistentDateRange;

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
			@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		System.out.println("RatesController.getRates()");
	}

}
