package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.CartDetailsResponseDTO;
import com.trash.ecommerce.dto.CartResponseDTO;
import com.trash.ecommerce.entity.CartItem;

public interface CartService {
    public CartDetailsResponseDTO getMyCart(String token);
    public CartResponseDTO createCart(String token);
    public CartResponseDTO addItemIntoCart(String token, CartItem cartItem);
    public CartResponseDTO removeItemOutOfCart(String token, Long cartItemId);
}
