package com.searchmetrics.exchangerates.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.searchmetrics.exchangerates.persistence.models.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long>, JpaSpecificationExecutor<ExchangeRate> {
	
	Optional<ExchangeRate> findTopByOrderByCreatedAtDesc();

}
