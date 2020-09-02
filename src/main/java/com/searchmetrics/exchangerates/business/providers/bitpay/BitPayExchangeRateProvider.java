package com.searchmetrics.exchangerates.business.providers.bitpay;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.searchmetrics.exchangerates.business.exceptions.BusinessException;
import com.searchmetrics.exchangerates.business.providers.CurrencyExchangeRateProvider;
import com.searchmetrics.exchangerates.config.AppProperties;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Component
@RequiredArgsConstructor
public class BitPayExchangeRateProvider implements CurrencyExchangeRateProvider {

	private final AppProperties config;

	@Override
	public Optional<BigDecimal> conversionRate(String from, String to) {
		return client()
		        .get()
		        .uri(uriBuilder -> uriBuilder.path("/rates/{from}/{to}").build(from, to))
		        .accept(APPLICATION_JSON)
		        .header("x-accept-version", "2.0.0")
		        .retrieve()
		        .onStatus(
		            HttpStatus::is4xxClientError,
		            response ->
		                Mono.error(
		                    new BusinessException(
		                        HttpStatus.BAD_REQUEST,
		                        "No exchange rate could be calculated for provided currencies!")))
		        .onStatus(
		                HttpStatus::is5xxServerError,
		                response ->
		                    Mono.error(
		                        new BusinessException(
		                            HttpStatus.BAD_GATEWAY,
		                            "Rate provider connection failing!")))
		        .bodyToMono(ApiResponse.class)
		        .log()
		        .blockOptional()
		        .map(r -> {
		        	
		        	if (r.getError().isPresent()) {
						throw new BusinessException(HttpStatus.BAD_GATEWAY, r.getError().get());
					}
		        	
		        	if (r.getData().isPresent()) {
		        		return Optional.ofNullable(r.getData().get().getRate());
		        	}
		        	
		        	throw new BusinessException(HttpStatus.BAD_GATEWAY, "Unexpected response from bitpay provider received!");
		        	
		        })
		        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_GATEWAY, "Unexpected response from bitpay provider received!"));
		
		
	}

	private WebClient client() {
	    TcpClient tcpClient =
	        TcpClient.create()
	            .option(
	                ChannelOption.CONNECT_TIMEOUT_MILLIS,
	                config.getProviders().getBlockchain().getTimeout().intValue())
	            .doOnConnected(
	                connection -> {
	                  connection.addHandlerLast(
	                      new ReadTimeoutHandler(
	                          config.getProviders().getBlockchain().getTimeout(),
	                          TimeUnit.MILLISECONDS));
	                  connection.addHandlerLast(
	                      new WriteTimeoutHandler(
	                          config.getProviders().getBlockchain().getTimeout(),
	                          TimeUnit.MILLISECONDS));
	                });
	    return WebClient.builder()
	        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
	        .baseUrl(config.getProviders().getBlockchain().getBaseurl())
	        .build();
	  }

}
