package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.CartItemTransactionalResponse;
import com.trash.ecommerce.service.CartService;
import com.trash.ecommerce.service.CartItemService;
import com.trash.ecommerce.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private Logger logger = LoggerFactory.getLogger(CartController.class);
    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/items")
    public ResponseEntity<List<CartItemDetailsResponseDTO>> getCartItems(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtService.extractId(token);
            List<CartItemDetailsResponseDTO> cartItems = cartService.getAllItemFromMyCart(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            logger.error("Get item has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartItemTransactionalResponse> updateCartItem(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestParam(value = "quantity", defaultValue = "1", required = false) Long quantity) {
        try {
            Long userId = jwtService.extractId(token);
            CartItemTransactionalResponse response = cartItemService.updateQuantityCartItem(userId, quantity, productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("add product failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartItemTransactionalResponse> removeCartItem(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        try {
            Long userId = jwtService.extractId(token);
            CartItemTransactionalResponse response = cartItemService.removeItemOutOfCart(userId, productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("delete product from cart has failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}