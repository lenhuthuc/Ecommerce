package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.ReviewRequest;
import com.trash.ecommerce.dto.ReviewResponse;
import com.trash.ecommerce.entity.Product;
import com.trash.ecommerce.entity.Review;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.exception.ReviewException;
import com.trash.ecommerce.mapper.ReviewsMapper;
import com.trash.ecommerce.repository.OrderRepository;
import com.trash.ecommerce.repository.ProductRepository;
import com.trash.ecommerce.repository.ReviewRepository;
import com.trash.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewsMapper reviewsMapper;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Override
    public ReviewResponse createComment(Long userId, Long productId, ReviewRequest reviewRequest) {
        if (reviewRequest == null) {
            throw new IllegalArgumentException("Review request cannot be null");
        }
        Review review = reviewsMapper.mapReviewDTO(reviewRequest);
        Users users = userRepository.findById(userId)
                .orElseThrow(() -> new FindingUserError("User not found"));
        Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductFingdingException("Product not found"));
        
        // Kiểm tra user đã mua sản phẩm chưa
        boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndStatusPaid(userId, productId);
        if (!hasPurchased) {
            throw new ReviewException("Bạn chỉ có thể đánh giá sản phẩm đã mua!");
        }
        if (users.getReviews() == null) {
            users.setReviews(new ArrayList<>());
        }
        users.getReviews().add(review);
        if (product.getReviews() == null) {
            product.setReviews(new ArrayList<>());
        }
        product.getReviews().add(review);
        review.setUser(users);
        review.setProduct(product);
        reviewRepository.save(review);
        return reviewsMapper.mapReview(review);
    }

    @Override
    public void deleteComment(Long userId, Long productId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("review not found"));
        if (review.getUser() == null || review.getProduct() == null) {
            throw new ReviewException("Review is missing user or product information");
        }
        if(Objects.equals(review.getUser().getId(), userId) && Objects.equals(review.getProduct().getId(), productId)) {
            reviewRepository.deleteById(reviewId);
        } else {
            throw new AccessDeniedException("You do not have permission to delete this review");
        }
    }

    @Override
    public List<ReviewResponse> findReviewByProductId(Long productId) {
        List<ReviewResponse> reviews = reviewRepository.findByProductId(productId)
                .stream()
                .map(review -> (ReviewResponse) reviewsMapper.mapReview(review))
                .toList();
        return reviews;
    }
}
