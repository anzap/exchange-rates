package com.searchmetrics.exchangerates.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

	@Bean
	public Docket docket(ApiInfo apiInfo) {
		return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo).select()
				.apis(RequestHandlerSelectors.basePackage("com.searchmetrics.exchangerates")).build();
	}

	@Bean
	public ApiInfo apiInfo(Contact contact) {
		return new ApiInfo("Exchange Rate Provider", "Exercise task for Searchmetrics", "0.0.1", "", contact, null,
				null, new ArrayList<>());
	}

	@Bean
	public Contact contact() {
		return new Contact("Andreas Zapantis", null, "antreaszapantis@gmail.com");
	}

}
