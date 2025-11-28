package com.trash.ecommerce.service;

import com.trash.ecommerce.exception.CartItemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.trash.ecommerce.dto.CartItemTransactionalResponse;
import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.CartItemId;
import com.trash.ecommerce.entity.Product;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.exception.ResourceNotFoundException;
import com.trash.ecommerce.repository.CartItemRepository;
import com.trash.ecommerce.repository.ProductRepository;
import com.trash.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired 
    private ProductRepository productRepository;
    @Override
    @Transactional
    public CartItemTransactionalResponse updateQuantityCartItem(Long userId, Long quantity, Long productId) {

        Users users = userRepository.findById(userId)
                                        .orElseThrow(() -> new FindingUserError("user is not found"));
        Cart cart = users.getCart();
        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new ProductFingdingException("product can't be found"));
        CartItemId cartItemId = new CartItemId(cart.getId(), productId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                                            .orElseGet(
                                                () -> {
                                                    CartItem anotherCartItem = new CartItem();
                                                    anotherCartItem.setId(cartItemId);
                                                    anotherCartItem.setCart(cart);
                                                    anotherCartItem.setProduct(product);
                                                    return anotherCartItem;
                                                }
                                            );
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return new CartItemTransactionalResponse("Add product into cart successful !");
    }

    @Override
    @Transactional
    public CartItemTransactionalResponse removeItemOutOfCart(Long userId, Long productId) {
        Users users = userRepository.findById(userId)
                                        .orElseThrow(() -> new FindingUserError("user is not found"));
        Cart cart = users.getCart();
        Long cartId = cart.getId();
        CartItem cartItem = cartItemRepository.findById(new CartItemId(cartId, productId))
                .orElseThrow(() -> new CartItemException("Item not found"));
        CartItemId cartItemId = cartItem.getId();
        if (!cartItemId.getCartId().equals(cartId)) {
            throw new AccessDeniedException("You can't delete this item !");
        }
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartItemRepository.delete(cartItem);
        return new CartItemTransactionalResponse("delete item successful");
    }

}
