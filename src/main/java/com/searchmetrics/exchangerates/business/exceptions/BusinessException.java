package com.searchmetrics.exchangerates.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BusinessException extends ResponseStatusException {

  private static final long serialVersionUID = 1L;

  public BusinessException(HttpStatus status, String reason) {
    this(status, reason, null);
  }

  public BusinessException(HttpStatus status, String reason, Throwable cause) {
    super(status, reason, cause);
  }
}