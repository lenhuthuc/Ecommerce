package com.trash.ecommerce.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartItemDetailsResponseDTO {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Long quantity;
}
