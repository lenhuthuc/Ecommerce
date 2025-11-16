package com.trash.ecommerce.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "status") 
    private String status;
    @Column(name = "total_price")
    private Double totalPrice;
    @Column(name = "create_at")
    private Date createAt;
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
    
}
