package com.trash.ecommerce.mapper;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.OrderRequest;
import com.trash.ecommerce.dto.OrderResponseDTO;
import com.trash.ecommerce.dto.OrderSummaryDTO;
import com.trash.ecommerce.entity.Order;
import com.trash.ecommerce.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderSummaryDTO toOrderSummaryDTO(Order order, String paymentUrl) {
        if (order == null) {
            return null;
        }
        
        OrderSummaryDTO orderSummaryDTO = new OrderSummaryDTO();
        orderSummaryDTO.setId(order.getId());
        orderSummaryDTO.setCreateAt(order.getCreateAt());
        // Xử lý an toàn cho status - có thể null hoặc invalid
        if (order.getStatus() != null) {
            orderSummaryDTO.setStatus(order.getStatus().name());
        } else {
            orderSummaryDTO.setStatus("UNKNOWN");
        }
        orderSummaryDTO.setTotalPrice(order.getTotalPrice());
        orderSummaryDTO.setPaymentMethodName(order.getPaymentMethod() != null ? order.getPaymentMethod().getMethodName() : null);
        orderSummaryDTO.setPaymentUrl(paymentUrl);
        // Xử lý an toàn cho orderItems - tránh lazy loading exception
        try {
            orderSummaryDTO.setTotalItems(order.getOrderItems() == null ? 0 : order.getOrderItems().size());
        } catch (Exception e) {
            // Nếu không thể load orderItems (lazy loading issue), set về 0
            orderSummaryDTO.setTotalItems(0);
        }

        return orderSummaryDTO;
    }

    public OrderResponseDTO toOrderResponseDTO(Order order, String paymentUrl) {
        if (order == null) {
            return null;
        }

        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setAddress(order.getAddress());
        dto.setPaymentUrl(paymentUrl);
        if (order.getOrderItems() != null) {
            Set<CartItemDetailsResponseDTO> itemDTOs = order.getOrderItems().stream()
                    .map(this::toCartItemDetailsResponseDTO)
                    .collect(Collectors.toSet());
            dto.setCartItems(itemDTOs);
        }



        return dto;
    }


    private CartItemDetailsResponseDTO toCartItemDetailsResponseDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        CartItemDetailsResponseDTO dto = new CartItemDetailsResponseDTO();
        if (orderItem.getProduct() != null) {
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getProductName());
        }
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());

        return dto;
    }

    public Order toOrderEntity(OrderRequest request) {
        if (request == null) {
            return null;
        }

        Order order = new Order();
        order.setStatus(request.getStatus());
        order.setTotalPrice(request.getTotalPrice());

        if (request.getCreateAt() != null) {
            Date date = Date.from(request.getCreateAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
            order.setCreateAt(date);
        } else {
            order.setCreateAt(new Date());
        }

        return order;
    }
}