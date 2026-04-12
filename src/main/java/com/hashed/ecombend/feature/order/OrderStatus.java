package com.hashed.ecombend.feature.order;

/**
 * Valid lifecycle states for an Order.
 * <p>
 * Valid transitions:
 * PENDING → CONFIRMED → SHIPPED → DELIVERED
 * Any state → CANCELLED
 * DELIVERED → REFUNDED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
