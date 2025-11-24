package com.trash.ecommerce.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String methodName;

    @ManyToMany(
        fetch = FetchType.LAZY, 
        cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH }, 
        mappedBy = "paymentMethods"
    )
    private Set<Users> users = new HashSet<>();

    @OneToMany(
        fetch = FetchType.LAZY, 
        cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH }, 
        mappedBy = "paymentMethod"
    )
    private Set<Invoice> invoices = new HashSet<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE,
                    CascadeType.DETACH, CascadeType.REFRESH },
            mappedBy = "paymentMethod"
    )
    private Set<Order> orders = new HashSet<>();
}