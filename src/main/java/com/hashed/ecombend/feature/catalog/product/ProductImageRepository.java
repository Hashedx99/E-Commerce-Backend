package com.hashed.ecombend.feature.catalog.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(UUID productId);

    /**
     * Counts existing images for a product used to set sort_order on new uploads.
     */
    int countByProductId(UUID productId);

    /**
     * Checks whether a primary image already exists for a product.
     */
    boolean existsByProductIdAndPrimaryTrue(UUID productId);
}
