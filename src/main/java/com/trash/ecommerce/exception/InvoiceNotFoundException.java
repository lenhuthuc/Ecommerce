package com.trash.ecommerce.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}