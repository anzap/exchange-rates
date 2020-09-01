package com.searchmetrics.exchangerates.controllers.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ConsistentDateRangeValidator.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConsistentDateRange {
  String message() default "Date range passed in request is invalid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
