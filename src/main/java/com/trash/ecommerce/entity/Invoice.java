package com.trash.ecommerce.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "invoice")
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

    @Column(name = "total_price")
    private Double price;

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

    public Invoice() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
