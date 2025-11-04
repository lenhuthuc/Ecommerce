package com.trash.ecommerce.service;

import com.trash.ecommerce.entity.PaymentMethod;

public interface PaymentMethodService {
    public PaymentMethod findPaymentMethod(String name);
}
