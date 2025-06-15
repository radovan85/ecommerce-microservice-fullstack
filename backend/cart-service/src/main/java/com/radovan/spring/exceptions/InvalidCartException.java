package com.radovan.spring.exceptions;

import javax.management.RuntimeErrorException;

public class InvalidCartException extends RuntimeErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCartException(Error e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

}
