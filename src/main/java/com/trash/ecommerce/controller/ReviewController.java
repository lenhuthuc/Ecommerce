package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.ReviewRequest;
import com.trash.ecommerce.dto.ReviewResponse;
import com.trash.ecommerce.service.JwtService;
import com.trash.ecommerce.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private Logger logger = LoggerFactory.getLogger(ReviewController.class);
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/products/{productId}")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody ReviewRequest reviewRequest) {
        try {
            Long userId = jwtService.extractId(token);
            ReviewResponse response = reviewService.createComment(userId, productId, reviewRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("createReview has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/products/{productId}/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        try {
            Long userId = jwtService.extractId(token);
            reviewService.deleteComment(userId, productId, reviewId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("deleteReview has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        try {
            List<ReviewResponse> reviews = reviewService.findReviewByProductId(productId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("getProductReviews has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}