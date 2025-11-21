package com.trash.ecommerce.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDetailsResponseDTO {
    private String img;
    private String product_name;
    private BigDecimal price;
    private Long quantity;
}
