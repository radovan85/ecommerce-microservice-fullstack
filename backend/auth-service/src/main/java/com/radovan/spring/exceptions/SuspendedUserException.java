package com.radovan.spring.exceptions;

import javax.management.RuntimeErrorException;

public class SuspendedUserException extends RuntimeErrorException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SuspendedUserException(Error e) {
        super(e);
        // TODO Auto-generated constructor stub
    }

}
