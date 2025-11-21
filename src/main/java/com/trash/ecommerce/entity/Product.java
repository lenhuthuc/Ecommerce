package com.trash.ecommerce.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "img", nullable = false)
    private String img;
    @Column(nullable = false)
    private String productName;

    @Column(name = "price",nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @OneToMany(
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,   
    mappedBy = "product")
    private Set<CartItem> cartItems;

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        mappedBy = "product"
    )
    private Set<InvoiceItem> invoiceItems;
    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "product",
        cascade = CascadeType.ALL
    )
    private List<Review> reviews;
    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        mappedBy = "product"
    )
    private Set<OrderItem> orderItems = new HashSet<>();
}