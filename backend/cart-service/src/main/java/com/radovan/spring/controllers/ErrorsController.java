package com.radovan.spring.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.exceptions.InvalidCartException;
import com.radovan.spring.exceptions.OperationNotAllowedException;
import com.radovan.spring.exceptions.OutOfStockException;

@RestControllerAdvice
public class ErrorsController {

	@ExceptionHandler(InstanceUndefinedException.class)
	public ResponseEntity<String> handleInstanceUndefinedException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(InvalidCartException.class)
	public ResponseEntity<String> handleInvalidCartException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(OutOfStockException.class)
	public ResponseEntity<String> handleOutOfStockException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(OperationNotAllowedException.class)
	public ResponseEntity<String> handleOperationNotAllowedException(Error error) {
		return new ResponseEntity<String>(error.getMessage(), HttpStatus.NOT_ACCEPTABLE);
	}
}
