package com.searchmetrics.exchangerates.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RatesController.class)
public class RatesControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void validRequestNoParams() throws Exception {

		mvc.perform(get("/api/rates")).andExpect(status().isOk());

	}

	@Test
	void validRequestDateParams() throws Exception {

		mvc.perform(get("/api/rates/snapshots").param("from", "2020-08-01")).andExpect(status().isOk());
		mvc.perform(get("/api/rates/snapshots").param("from", "2020-07-01").param("to", "2020-08-01")).andExpect(status().isOk());

	}

	@Test
	void invalidRequestDateParams() throws Exception {
		mvc.perform(get("/api/rates/snapshots").param("to", "2020-08-01")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is("BAD_REQUEST")))
				.andExpect(jsonPath("$.time", is(notNullValue())))
				.andExpect(jsonPath("$.path", is("/api/rates/snapshots")))
				.andExpect(jsonPath("$.method", is("GET")))
				.andExpect(jsonPath("$.errors[0].message", is("Date range passed in request is invalid")));

		mvc.perform(get("/api/rates/snapshots").param("from", "2020-09-01").param("to", "2020-08-01"))
				.andExpect(jsonPath("$.status", is("BAD_REQUEST")))
				.andExpect(jsonPath("$.time", is(notNullValue())))
				.andExpect(jsonPath("$.path", is("/api/rates/snapshots")))
				.andExpect(jsonPath("$.method", is("GET")))
				.andExpect(jsonPath("$.errors[0].message", is("Date range passed in request is invalid")));

	}

}
