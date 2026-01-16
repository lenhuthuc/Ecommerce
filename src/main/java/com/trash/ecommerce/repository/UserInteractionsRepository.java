package com.trash.ecommerce.repository;

import com.trash.ecommerce.entity.UserInteractions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionsRepository extends JpaRepository<UserInteractions, Long> {
    List<UserInteractions> findByUserId(Long userId);
    List<UserInteractions> findByProductId(Long productId);
    List<UserInteractions> findByUserIdAndProductId(Long userId, Long productId);
}
