package com.radovan.play.exceptions;

public class OutOfStockException extends IllegalStateException {
    public OutOfStockException(String s) {
        super(s);
    }
}
