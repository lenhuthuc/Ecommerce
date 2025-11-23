package com.trash.ecommerce.mapper;

import com.trash.ecommerce.dto.ReviewRequest;
import com.trash.ecommerce.dto.ReviewResponse;
import com.trash.ecommerce.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewsMapper {
    public ReviewResponse mapReview (Review review) {
        return new ReviewResponse(
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
