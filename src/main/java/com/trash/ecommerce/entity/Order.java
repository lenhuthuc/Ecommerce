package com.trash.ecommerce.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;
    @Column(name = "status") 
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(name = "total_price",nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
    @Column(name = "created_at")
    private Date createAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        mappedBy = "order"
    )
    private Set<OrderItem> orderItems = new HashSet<>();

    @OneToOne(
            cascade = CascadeType.ALL,
            mappedBy = "order"
    )
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentMethod paymentMethod;

    @Column(name = "address")
    private String address;
}
