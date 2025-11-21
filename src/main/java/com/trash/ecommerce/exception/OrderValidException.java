package com.trash.ecommerce.exception;

public class OrderValidException extends RuntimeException {
    public OrderValidException(String message) {
        super(message);
    }
}
