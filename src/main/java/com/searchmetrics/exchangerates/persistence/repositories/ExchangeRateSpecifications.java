package com.searchmetrics.exchangerates.persistence.repositories;

import java.time.Instant;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.searchmetrics.exchangerates.persistence.models.ExchangeRate;
import com.searchmetrics.exchangerates.persistence.models.ExchangeRate_;

public class ExchangeRateSpecifications {
	
	public static Specification<ExchangeRate> fromCurrency(String currency) {

		return new Specification<ExchangeRate>() {

			@Override
			public Predicate toPredicate(Root<ExchangeRate> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get(ExchangeRate_.fromCurrency), currency);
			}
		};
	}
	
	public static Specification<ExchangeRate> toCurrency(String currency) {

		return new Specification<ExchangeRate>() {

			@Override
			public Predicate toPredicate(Root<ExchangeRate> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get(ExchangeRate_.toCurrency), currency);
			}
		};
	}

	public static Specification<ExchangeRate> after(Instant date) {

		return new Specification<ExchangeRate>() {

			@Override
			public Predicate toPredicate(Root<ExchangeRate> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.greaterThanOrEqualTo(root.get(ExchangeRate_.createdAt), date);
			}
		};
	}
	
	public static Specification<ExchangeRate> before(Instant date) {

		return new Specification<ExchangeRate>() {

			@Override
			public Predicate toPredicate(Root<ExchangeRate> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.lessThanOrEqualTo(root.get(ExchangeRate_.createdAt), date);
			}
		};
	}

}
