package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.CartItemTransactionalResponse;
import com.trash.ecommerce.entity.CartItemId;

public interface CartItemService {
    public CartItemTransactionalResponse updateQuantityCartItem(Long userId, Long quantity, Long productId);
    public CartItemTransactionalResponse removeItemOutOfCart(Long userId, Long productId);

}
