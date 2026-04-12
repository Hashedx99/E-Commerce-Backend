package com.hashed.ecombend.feature.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashed.ecombend.common.entity.BaseEntity;
import com.hashed.ecombend.feature.catalog.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single line item inside an Order.
 * CRITICAL DESIGN DECISIONS:
 * 1. product_id is nullable if a product is soft deleted after the order
 * was placed, the order item must still be readable. The product_id FK
 * points to a deleted (invisible) record, but the snapshot fields preserve
 * what the customer actually ordered.
 * 2. productName and productSku are snapshotted at order time if the product
 * name changes later, historical orders still show the correct name from the time of purchase.
 * 3. unitPrice is snapshotted the customer is charged the price at order time, not the current price. This matches a
 * real receipt.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Nullable product may be soft deleted after this order was placed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    /**
     * Expose product id without loading the entity.
     */
    @Column(name = "product_id", insertable = false, updatable = false)
    private UUID productId;

    /**
     * Snapshot of product name at time of order.
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Snapshot of product SKU at time of order.
     */
    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /**
     * Snapshot of unit price at time of order.
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
