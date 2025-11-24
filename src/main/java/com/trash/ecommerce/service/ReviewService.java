package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.ReviewRequest;
import com.trash.ecommerce.dto.ReviewResponse;
import com.trash.ecommerce.entity.Review;
import com.trash.ecommerce.repository.ReviewRepository;

import java.util.List;

public interface ReviewService {
    public ReviewResponse createComment(Long userId, Long productId, ReviewRequest review);
    public void deleteComment(Long userId, Long productId, Long reviewId);
    public List<ReviewResponse> findReviewByProductId(Long productId);
}
