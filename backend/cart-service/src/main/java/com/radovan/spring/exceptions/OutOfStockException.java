package com.radovan.spring.exceptions;

import javax.management.RuntimeErrorException;

public class OutOfStockException extends RuntimeErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OutOfStockException(Error e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

}
