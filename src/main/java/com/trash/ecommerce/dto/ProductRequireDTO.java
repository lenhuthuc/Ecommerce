package com.trash.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductRequireDTO {
    @NotNull
    private String img;
    @NotNull
    private String productName;
    @NotNull
    private Double price;
    @NotNull
    private Long quantity;
}
