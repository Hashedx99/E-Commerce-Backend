package com.hashed.ecombend.feature.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * all non-deleted reviews for a product, newest first.
     */
    List<Review> findByProductIdOrderByCreatedAtDesc(UUID productId);

    /**
     * Used to check ownership before edit/delete.
     */
    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Checks if a user has already reviewed a product (one review per user per product).
     */
    boolean existsByProductIdAndUserId(UUID productId, UUID userId);
}
