package com.trash.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReviewResponse {
    private Long userId;
    private Long productId;
    private Rating rate;
    private String comment;
}
