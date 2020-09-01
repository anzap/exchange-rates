package com.searchmetrics.exchangerates.api.errors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
class ApiError {

	private HttpStatus status;
	private Instant time;
	private String path;
	private String method;
	private List<ApiSubError> errors;

	private ApiError() {
		time = Instant.now();
	}

	ApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	public static ApiError fromDefaultAttributeMap(WebRequest request,
			final Map<String, Object> defaultErrorAttributes) {
		HttpServletRequest httpReq = ((ServletWebRequest) request).getRequest();
		ApiError apiError = new ApiError();
		apiError.setStatus(HttpStatus.valueOf((Integer) defaultErrorAttributes.get("status")));
		apiError.setPath((String) defaultErrorAttributes.getOrDefault("path", "no path info available"));
		apiError.setMethod(httpReq.getMethod());
		apiError.addSubError(new ApiValidationError((String) defaultErrorAttributes.get("message")));
		return apiError;
	}

	public Map<String, Object> toAttributeMap() {
		return Map.of("status", getStatus(), "time", getTime(), "path", getPath(), "method", getMethod(), "errors",
				getErrors());
	}

	private void addSubError(ApiSubError subError) {
		if (errors == null) {
			errors = new ArrayList<>();
		}
		errors.add(subError);
	}

	void addValidationError(String message) {
		addSubError(new ApiValidationError(message));
	}

	void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
		constraintViolations.forEach(this::addValidationError);
	}

	private void addValidationError(ConstraintViolation<?> cv) {
		this.addValidationError(cv.getMessage());
	}

	void addValidationErrors(List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	private void addValidationError(FieldError fieldError) {
		this.addValidationError(fieldError.getDefaultMessage());
	}

	interface ApiSubError {
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor
	static class ApiValidationError implements ApiSubError {
		private String message;
	}
}
