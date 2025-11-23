package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.CartItemTransactionalResponse;
import com.trash.ecommerce.service.CartItemService;
import com.trash.ecommerce.service.CartService;
import com.trash.ecommerce.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    @Autowired
    private CartService cartService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private JwtService jwtService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/")
    public ResponseEntity<List<CartItemDetailsResponseDTO>> getAllItem(
            @RequestHeader("Authorization") String token
    ) {
        try {
            Long userId = jwtService.extractId(token);
            if (userId != null) {
                List<CartItemDetailsResponseDTO> cart = cartService.getAllItemFromMyCart(userId);
                return ResponseEntity.ok(cart);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lấy giỏ hàng: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/push/{id}")
    public ResponseEntity<CartItemTransactionalResponse> pushProductToCart(
            @RequestHeader("token") String token,
            @RequestParam(value = "quantity", defaultValue = "1") Long quantity,
            @PathVariable("id") Long id
    ) {
        try {
            Long userId = jwtService.extractId(token);
            if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            CartItemTransactionalResponse item = cartItemService.updateQuantityCartItem(userId, quantity, id);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            logger.error("Error when push product into cart",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CartItemTransactionalResponse> deleteProductToCart(
            @RequestHeader("token") String token,
            @PathVariable("id") Long id
    ) {
        try {
            Long userId = jwtService.extractId(token);
            if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            CartItemTransactionalResponse item = cartItemService.removeItemOutOfCart(userId, id);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            logger.error("Error when push product into cart",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
