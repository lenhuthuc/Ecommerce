package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.CartItemDetailsResponse;

public interface CartItemService {
    public CartItemDetailsResponse updateQuantityCartItem(String token, Long quantity, Long productId);
    public CartItemDetailsResponse removeItemOutOfCart(String token, Long cartItemId);

}
