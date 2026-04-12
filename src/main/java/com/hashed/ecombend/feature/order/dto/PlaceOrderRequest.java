package com.hashed.ecombend.feature.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * request body for POST /api/orders.
 */
@Data
public class PlaceOrderRequest {

    /**
     * Address ID from the user's saved addresses.
     */
    private UUID addressId;

    /**
     * Optional coupon code handled by CouponService (stub for now).
     */
    private String couponCode;

    private String notes;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }
}
