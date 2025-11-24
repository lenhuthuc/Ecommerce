package com.trash.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "invoice_items")
public class InvoiceItem implements Serializable {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private InvoiceItemId id = new InvoiceItemId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("invoiceId") 
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId") 
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Long quantity = 1L;

   @Column(name = "price",nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

   @Column(name = "total")
   private BigDecimal total;
}
