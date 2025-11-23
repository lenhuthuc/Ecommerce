package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;

public interface OrderService {
    public OrderResponseDTO createMyOrder(Long userId, Long paymentMethodId);
    public OrderMessageResponseDTO deleteOrder(Long userId, Long orderId);
    public OrderMessageResponseDTO checkOutOrder(Long userId, Long orderId, String ipAddress);
    public OrderMessageResponseDTO finalizeOrder(Long userId, Long orderId);
}
