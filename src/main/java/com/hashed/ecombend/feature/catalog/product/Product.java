package com.hashed.ecombend.feature.catalog.product;

import com.hashed.ecombend.common.entity.SoftDeleteEntity;
import com.hashed.ecombend.feature.catalog.category.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * a catalog product.
 *
 * @Version on stock enables optimistic locking for concurrency
 * Use decrementStock() / incrementStock() — never set stock directly.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug", unique = true),
        @Index(name = "idx_product_sku", columnList = "sku", unique = true),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends SoftDeleteEntity {

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * slug generated from the name. Unique.
     */
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Stock Keeping Unit.
     */
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * The crossed out "was" price shown during a sale.
     * Optional null means product is not on sale.
     * Must be greater than price if set.
     */
    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    /**
     * Current stock count. Protected by @Version for concurrency control.
     * Never modify this field directly use decrementStock() / incrementStock().
     */
    @Column(name = "stock", nullable = false)
    private int stock = 0;

    /**
     * Alert threshold. When stock falls at or below this value, isLowStock() returns true.
     */
    @Column(name = "low_stock_threshold")
    private int lowStockThreshold = 5;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
