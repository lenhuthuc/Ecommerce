package com.trash.ecommerce.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trash.ecommerce.entity.Product;
@Repository
public interface ProductRepository extends JpaRepository <Product, Long> {
    @Query(
        value = " SELECT * FROM product " + 
        "WHERE product_name LIKE CONCAT('%', :name, '%') ",
        nativeQuery = true
    )
    Page<Product> findProductsByName(@Param("name") String name, PageRequest pageRequest);
}
