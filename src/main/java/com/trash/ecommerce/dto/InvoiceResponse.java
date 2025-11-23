package com.trash.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceResponse {
    private Long id;
    private Long userId;
    private Long orderId;
    private BigDecimal totalAmount;
    private Date createdAt;
}
