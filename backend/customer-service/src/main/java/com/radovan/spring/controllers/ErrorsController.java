package com.radovan.spring.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import com.radovan.spring.exceptions.DataNotValidatedException;
import com.radovan.spring.exceptions.ExistingInstanceException;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.exceptions.SuspendedUserException;

@RestControllerAdvice
public class ErrorsController {

	@ExceptionHandler(DataNotValidatedException.class)
	public ResponseEntity<String> handleDataNotValidatedException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(InstanceUndefinedException.class)
	public ResponseEntity<String> handleInstanceUndefinedException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<String> handleHttpClientErrorException(HttpClientErrorException ex) {
		return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
	}

	@ExceptionHandler(ExistingInstanceException.class)
	public ResponseEntity<String> handleExistingInstanceException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(SuspendedUserException.class)
	public ResponseEntity<String> handleSuspendedUserException(Error error) {
		SecurityContextHolder.clearContext();
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
	}
}
