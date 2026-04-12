package com.hashed.ecombend.feature.review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashed.ecombend.common.entity.SoftDeleteEntity;
import com.hashed.ecombend.feature.catalog.product.Product;
import com.hashed.ecombend.feature.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * a customer review of a product.
 * Soft-deleted so admins can remove reviews without losing the data audit trail.
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review extends SoftDeleteEntity {

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "title")
    private String title;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    /**
     * Automatically set to true when the reviewer has a DELIVERED order
     * that contains this product.
     */
    @Column(name = "is_verified_purchase", nullable = false)
    private boolean verifiedPurchase = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Expose IDs without loading the full entity
    @Column(name = "user_id", insertable = false, updatable = false)
    private java.util.UUID userId;

    @Column(name = "product_id", insertable = false, updatable = false)
    private java.util.UUID productId;
}
