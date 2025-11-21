package com.trash.ecommerce.exception;

public class OrderExistsException extends RuntimeException {
    public OrderExistsException(String message) {
        super(message);
    }
}
