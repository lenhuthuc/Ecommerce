package com.trash.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "invoice")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        cascade = {
            CascadeType.PERSIST, 
            CascadeType.MERGE, 
            CascadeType.DETACH, 
            CascadeType.REFRESH
        }
    )
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "total_price",nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(
        fetch = FetchType.LAZY,
        cascade = {
            CascadeType.PERSIST, 
            CascadeType.MERGE, 
            CascadeType.DETACH, 
            CascadeType.REFRESH
        }
    )
    @JoinColumn(name = "payment_id")
    private PaymentMethod paymentMethod;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
}
