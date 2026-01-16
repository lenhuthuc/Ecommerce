package com.trash.ecommerce.service;

import com.trash.ecommerce.entity.UserInteractions;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.entity.Product;
import com.trash.ecommerce.repository.UserInteractionsRepository;
import com.trash.ecommerce.repository.UserRepository;
import com.trash.ecommerce.repository.ProductRepository;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductFingdingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserInteractionService {

    @Autowired
    private UserInteractionsRepository userInteractionsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public UserInteractions recordInteraction(Long userId, Long productId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new FindingUserError("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductFingdingException("Product not found"));

        UserInteractions interaction = new UserInteractions();
        interaction.setUser(user);
        interaction.setProduct(product);
        interaction.setCreatedAt(LocalDateTime.now());

        return userInteractionsRepository.save(interaction);
    }

    public List<UserInteractions> getUserInteractions(Long userId) {
        return userInteractionsRepository.findByUserId(userId);
    }

    public List<UserInteractions> getProductInteractions(Long productId) {
        return userInteractionsRepository.findByProductId(productId);
    }

    public List<UserInteractions> getUserProductInteractions(Long userId, Long productId) {
        return userInteractionsRepository.findByUserIdAndProductId(userId, productId);
    }
}
