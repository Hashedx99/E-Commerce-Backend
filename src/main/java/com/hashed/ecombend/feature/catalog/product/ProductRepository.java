package com.hashed.ecombend.feature.catalog.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for Product entities.
 *
 * @SQLRestriction on Product means all methods auto-exclude soft-deleted products.
 * <p>
 * The existsByCategoryIdAndDeletedAtIsNull method is referenced by CategoryServiceImpl
 * to guard against deleting a category that still has active products.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    /**
     * Checks if active (non-deleted) products exist in a given category.
     * Used by CategoryServiceImpl.delete() to prevent orphaning products.
     * The "deletedAt IS NULL" clause is explicit here because the @SQLRestriction
     * applies to entity loads but not to exists() checks in all JPA providers.
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    boolean existsByCategoryIdAndDeletedAtIsNull(@Param("categoryId") UUID categoryId);

    /**
     * Paginated product list with optional category and price filters.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> findFiltered(
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
