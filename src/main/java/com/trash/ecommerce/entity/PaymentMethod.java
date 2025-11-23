package com.trash.ecommerce.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String methodName;

    @ManyToMany(
        fetch = FetchType.LAZY, 
        cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH }, 
        mappedBy = "paymentMethods"
    )
    private Set<Users> users;

    @OneToMany(
        fetch = FetchType.LAZY, 
        cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH }, 
        mappedBy = "paymentMethod"
    )
    private Set<Invoice> invoices;

    @OneToOne(
            fetch = FetchType.LAZY,
            mappedBy = "paymentMethod"
    )
    private Set<Order> orders;
}