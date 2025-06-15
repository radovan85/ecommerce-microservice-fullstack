package com.radovan.spring.exceptions;

import javax.management.RuntimeErrorException;

public class ExistingInstanceException extends RuntimeErrorException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ExistingInstanceException(Error e) {
        super(e);
        // TODO Auto-generated constructor stub
    }

}