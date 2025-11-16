package com.trash.ecommerce.dto;

import java.util.Set;

import com.trash.ecommerce.entity.CartItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartDetailsResponseDTO {
    Set<CartItem> cartItems;
}
