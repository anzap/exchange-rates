package com.searchmetrics.exchangerates.controllers.validators;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class ConsistentDateRangeValidator
    implements ConstraintValidator<ConsistentDateRange, Object[]> {

  @Override
  public boolean isValid(Object[] value, ConstraintValidatorContext context) {
    LocalDate from = (LocalDate) value[0];
    LocalDate to = (LocalDate) value[1];
    
    // We do not support defining just the end date
    if (from == null && to != null) {
    	return false;
    }
    
    // From date should be before to date
    if (to != null && from.compareTo(to) > 0) {
    	return false;
    }

    return true;
  }
}
