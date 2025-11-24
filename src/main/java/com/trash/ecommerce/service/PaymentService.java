package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.util.Map;

import com.trash.ecommerce.dto.OrderRequest;
import com.trash.ecommerce.dto.PaymentMethodMessageResponse;
import com.trash.ecommerce.dto.PaymentMethodResponse;
import com.trash.ecommerce.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    public PaymentMethodMessageResponse addPaymentMethod(Long userId, String name);
    public String createPaymentUrl(BigDecimal total_price, String orderInfo, Long orderId, String ipAddress);
    public PaymentMethodResponse handleProcedurePayment(HttpServletRequest request);
    public Map<String, String> hashFields(HttpServletRequest request);
    public PaymentMethodMessageResponse handleProcedureUserInterface(HttpServletRequest request);
}
