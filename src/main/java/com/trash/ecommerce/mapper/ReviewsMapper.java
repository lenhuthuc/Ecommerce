package com.trash.ecommerce.mapper;

import com.trash.ecommerce.dto.ReviewRequest;
import com.trash.ecommerce.dto.ReviewResponse;
import com.trash.ecommerce.entity.Review;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReviewsMapper {
    @Autowired
    private UserRepository userRepository;
    public ReviewResponse mapReview (Review review) {
        Users users = userRepository.findById(review.getUser().getId()).get();
        return new ReviewResponse(
                users.getUsername(),
                review.getProduct().getId(),
                review.getRating(),
                review.getContent()
        );
    }

    public Review mapReviewDTO (ReviewRequest review) {
        Review review1 = new Review();
        review1.setRating(review.getRate());
        review1.setContent(review.getComment());
        return review1;
    }
}
