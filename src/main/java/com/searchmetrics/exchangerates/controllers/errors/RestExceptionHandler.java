package com.searchmetrics.exchangerates.controllers.errors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(javax.validation.ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(
      javax.validation.ConstraintViolationException ex, WebRequest request) {
    log.error(ex.getMessage(), ex);
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.addValidationErrors(ex.getConstraintViolations());
    return buildResponseEntity(apiError, request);
  }

  private ResponseEntity<Object> buildResponseEntity(ApiError apiError, WebRequest request) {
    HttpServletRequest httpReq = ((ServletWebRequest) request).getRequest();
    apiError.setMethod(httpReq.getMethod());
    apiError.setPath(httpReq.getRequestURI());

    return new ResponseEntity<>(apiError, apiError.getStatus());
  }
}
