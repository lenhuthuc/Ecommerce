package com.trash.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.CartItemId;

public interface CartItemRepository extends JpaRepository <CartItem, CartItemId> {

}
