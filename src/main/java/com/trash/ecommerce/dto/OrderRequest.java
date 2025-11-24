package com.trash.ecommerce.dto;

import com.trash.ecommerce.entity.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderRequest {
    private OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createAt;
    private Long userId;
    private Long paymentMethodId;
}
