package com.trash.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trash.ecommerce.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository <Review, Long>{
    List<Review> findByProductId(Long productId);
}
