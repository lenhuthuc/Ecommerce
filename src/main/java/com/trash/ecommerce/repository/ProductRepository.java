package com.trash.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trash.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository <Product, Long> {

}
