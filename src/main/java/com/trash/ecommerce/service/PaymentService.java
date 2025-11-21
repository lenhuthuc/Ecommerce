package com.trash.ecommerce.service;

import java.math.BigDecimal;

import com.trash.ecommerce.dto.PaymentMethodResponse;
import com.trash.ecommerce.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    public String createPaymentUrl(BigDecimal total_price, String orderInfo, Order order, String ipAddress);
    public PaymentMethodResponse handleProcedurePayment(HttpServletRequest request);
}
