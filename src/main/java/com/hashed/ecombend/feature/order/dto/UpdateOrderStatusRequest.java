package com.hashed.ecombend.feature.order.dto;

import com.hashed.ecombend.feature.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * request body for PUT /api/admin/orders/{id}/status.
 */
@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
