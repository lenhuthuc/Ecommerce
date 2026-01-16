package com.trash.ecommerce.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductRequestDTO {
    @NotNull
    private String productName;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Long quantity;
    private String category;
    private String description;
}

