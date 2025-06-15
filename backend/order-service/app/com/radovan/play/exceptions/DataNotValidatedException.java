package com.radovan.play.exceptions;

public class DataNotValidatedException extends IllegalStateException {
    public DataNotValidatedException(String message) {
        super(message);
    }
}
