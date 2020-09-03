package com.searchmetrics.exchangerates.persistence.models;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "exhange_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExchangeRate extends PersistentEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "provider_name", nullable = false)
	@NotNull
	private String providerName;

	@Column(name = "from_currency", nullable = false)
	@NotNull
	private String fromCurrency;

	@Column(name = "to_currency", nullable = false)
	@NotNull
	private String toCurrency;

	@Column(name = "exchange_rate", nullable = false)
	@NotNull
	private BigDecimal exchangeRate;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof ExchangeRate))
			return false;

		ExchangeRate other = (ExchangeRate) o;

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return 31;
	}

}
