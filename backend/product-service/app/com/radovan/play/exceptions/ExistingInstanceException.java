package com.radovan.play.exceptions;

public class ExistingInstanceException extends IllegalStateException {
    public ExistingInstanceException(String message) {
        super(message);
    }
}