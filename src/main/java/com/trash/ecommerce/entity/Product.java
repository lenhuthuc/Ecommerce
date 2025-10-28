package com.trash.ecommerce.entity;

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

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Long quantity;

    @OneToMany(
    fetch = FetchType.LAZY,
    cascade = {CascadeType.PERSIST, CascadeType.MERGE, 
                CascadeType.DETACH, CascadeType.REFRESH},   
    mappedBy = "product")
    private Set<CartItem> cartItems;

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        mappedBy = "product"
    )
    private Set<InvoiceItem> invoiceItems;

    public Product(Long id, String productName, Double price, Long quantity, Set<CartItem> cartItems,
            Set<InvoiceItem> invoiceItems) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.cartItems = cartItems;
        this.invoiceItems = invoiceItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Set<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Set<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(Set<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }
}