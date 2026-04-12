package com.hashed.ecombend.feature.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * all orders for the current user, newest first.
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * all orders for admin view, paginated.
     */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Checks whether a user has a DELIVERED order that contains a specific product.
     * Used by ReviewServiceImpl to auto-set the isVerifiedPurchase flag.
     */
    @Query("""
            SELECT COUNT(o) > 0 FROM Order o
            JOIN o.items i
            WHERE o.user.id   = :userId
              AND o.status     = :status
              AND i.productId  = :productId
            """)
    boolean existsByUserIdAndStatusAndItems_ProductId(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            @Param("productId") UUID productId
    );

    /**
     * Verifies ownership used before returning order detail.
     */
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
}
