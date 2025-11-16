package com.trash.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDetailsResponseDTO {
    private String img;
    private String product_name;
    private Double price;
    private Long quantity;
}
