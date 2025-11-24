package com.trash.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "invoice")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            mappedBy = "invoice"
    )
    private Set<InvoiceItem> invoiceItems = new HashSet<>();
}
