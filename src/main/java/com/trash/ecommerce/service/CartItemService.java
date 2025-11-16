package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.CartItemDetailsResponse;

public interface CartItemService {
    public CartItemDetailsResponse updateQuantityCartItem(String token, int quantity, Long productId);
}
