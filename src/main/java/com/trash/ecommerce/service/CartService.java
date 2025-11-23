package com.trash.ecommerce.service;

import java.util.List;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;

public interface CartService {
    public List<CartItemDetailsResponseDTO> getAllItemFromMyCart(Long userId);
}
