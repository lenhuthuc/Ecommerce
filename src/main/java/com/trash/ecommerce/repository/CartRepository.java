package com.trash.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // <--- SỬA IMPORT NÀY
import org.springframework.stereotype.Repository;
import com.trash.ecommerce.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Modifying
    @Query(
            value = "DELETE FROM cart_items WHERE cart_id = :id", // Bỏ alias CI, SQL chuẩn
            nativeQuery = true
    )
    int deleteCartItems(@Param("id") Long id);
}