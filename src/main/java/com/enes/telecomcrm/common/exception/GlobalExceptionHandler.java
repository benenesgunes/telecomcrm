package com.enes.telecomcrm.common.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.enes.telecomcrm.common.dto.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
		return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
		return error(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining("; "));

		return error(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
		String message = ex.getConstraintViolations()
				.stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.collect(Collectors.joining("; "));

		return error(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
	}

	private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(ApiResponse.error(message));
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
