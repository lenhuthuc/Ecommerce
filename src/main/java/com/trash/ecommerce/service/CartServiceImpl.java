package com.trash.ecommerce.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.Product;
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
        if (cart == null) {
            throw new FindingUserError("Cart not found for user");
        }
        Set<CartItem> items = cart.getItems();
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<CartItemDetailsResponseDTO> cartItems = items.stream()
            .filter(item -> item != null && item.getProduct() != null)
            .map(item -> {
                CartItemDetailsResponseDTO cartDetails = new CartItemDetailsResponseDTO();
                Product product = item.getProduct();
                cartDetails.setProductId(product.getId());
                cartDetails.setProductName(product.getProductName());
                cartDetails.setPrice(product.getPrice());
                cartDetails.setQuantity(item.getQuantity());
                return cartDetails;
            })
            .toList();
        return cartItems;
    }

}
