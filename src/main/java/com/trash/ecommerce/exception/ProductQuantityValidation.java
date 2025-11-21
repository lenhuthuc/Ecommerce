package com.trash.ecommerce.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ProductQuantityValidation extends RuntimeException {
    public ProductQuantityValidation(String message) {
        super(message);
    }
}
