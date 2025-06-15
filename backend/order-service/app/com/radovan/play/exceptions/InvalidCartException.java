package com.radovan.play.exceptions;

public class InvalidCartException extends IllegalStateException {
    public InvalidCartException(String message) {
        super(message);
    }
}
