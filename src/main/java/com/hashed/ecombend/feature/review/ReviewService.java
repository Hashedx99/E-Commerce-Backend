package com.hashed.ecombend.feature.review;

import com.hashed.ecombend.feature.review.dto.ReviewRequest;
import com.hashed.ecombend.feature.review.dto.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    /**
     * returns all reviews for a product.
     */
    List<ReviewResponse> getForProduct(UUID productId);

    /**
     * customer creates a review. One review per user per product.
     */
    ReviewResponse create(UUID productId, ReviewRequest request);

    /**
     * customer edits their own review.
     */
    ReviewResponse update(UUID reviewId, ReviewRequest request);

    /**
     * customer deletes their own review; admin can delete any.
     */
    void delete(UUID reviewId);
}
