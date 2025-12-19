package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;
import com.trash.ecommerce.dto.OrderSummaryDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface OrderService {
    public List<OrderSummaryDTO> getAllMyOrders(Long userId, String IpAddress);
    public OrderResponseDTO getOrderById(Long userId, Long orderId, String IpAddress);
    public OrderResponseDTO createMyOrder(Long userId, Long paymentMethodId, String IpAddress);
    public OrderMessageResponseDTO deleteOrder(Long userId, Long orderId);

}
