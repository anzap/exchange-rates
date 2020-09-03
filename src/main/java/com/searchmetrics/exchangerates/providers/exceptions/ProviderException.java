package com.searchmetrics.exchangerates.providers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProviderException extends ResponseStatusException {

  private static final long serialVersionUID = 1L;

  public ProviderException(HttpStatus status, String reason) {
    this(status, reason, null);
  }

  public ProviderException(HttpStatus status, String reason, Throwable cause) {
    super(status, reason, cause);
  }
}