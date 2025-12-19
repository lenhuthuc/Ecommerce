package com.trash.ecommerce.dto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.trash.ecommerce.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderResponseDTO {
    private Set<CartItemDetailsResponseDTO> cartItems = new HashSet<>();
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String address;
    private String paymentUrl;
}
