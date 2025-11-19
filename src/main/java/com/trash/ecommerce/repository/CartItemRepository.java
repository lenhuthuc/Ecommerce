package com.trash.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.CartItemId;

public interface CartItemRepository extends JpaRepository <CartItem, CartItemId> {

}
