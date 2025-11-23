package com.trash.ecommerce.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.repository.UserRepository;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Override
    public List<CartItemDetailsResponseDTO> getAllItemFromMyCart(Long userId) {
        Users users = userRepository.findById(userId)
                                    .orElseThrow(() -> new FindingUserError("User not found"));
        Cart cart = users.getCart();
        Set<CartItem> items = cart.getItems();
        List<CartItemDetailsResponseDTO> cartItems = items.stream().map(
            item -> {
                CartItemDetailsResponseDTO cartDetails = new CartItemDetailsResponseDTO();
                cartDetails.setProductName(item.getProduct().getProductName());
                cartDetails.setPrice(item.getProduct().getPrice());
                cartDetails.setQuantity(item.getQuantity());
                return cartDetails;
            }
        ).toList();
        return cartItems;
    }

}
