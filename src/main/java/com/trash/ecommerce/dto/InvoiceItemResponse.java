package com.trash.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceItemResponse {
    private Long invoiceId;
    private Long productId;
    private Long quantity;
    private BigDecimal price;
    private BigDecimal total;
}
