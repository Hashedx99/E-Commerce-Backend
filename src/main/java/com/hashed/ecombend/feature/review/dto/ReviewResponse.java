package com.hashed.ecombend.feature.review.dto;

import com.hashed.ecombend.feature.review.Review;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewResponse {

    private UUID id;
    private int rating;
    private String title;
    private String comment;
    private boolean verifiedPurchase;
    private String reviewerName;
    private UUID productId;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review r) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(r.getId());
        dto.setRating(r.getRating());
        dto.setTitle(r.getTitle());
        dto.setComment(r.getComment());
        dto.setVerifiedPurchase(r.isVerifiedPurchase());
        dto.setProductId(r.getProductId());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getUser() != null) dto.setReviewerName(r.getUser().getName());
        return dto;
    }
}
