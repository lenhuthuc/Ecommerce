package com.trash.ecommerce.exception;

public class FindingUserError extends RuntimeException {
    public FindingUserError(String message) {
        super(message);
    }
}
