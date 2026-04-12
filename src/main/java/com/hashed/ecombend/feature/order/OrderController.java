package com.hashed.ecombend.feature.order;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.order.dto.PlaceOrderRequest;
import com.hashed.ecombend.feature.order.dto.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Orders", description = "Order placement and management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place a new order [CUSTOMER]")
    public ApiResponse<Order> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.ok("Order placed successfully", orderService.placeOrder(request));
    }

    @GetMapping("/api/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my order history [CUSTOMER]")
    public ApiResponse<List<Order>> getMyOrders() {
        return ApiResponse.ok("Orders retrieved", orderService.getMyOrders());
    }

    @GetMapping("/api/orders/{id}")
    @Operation(summary = "Get a specific order [CUSTOMER own | ADMIN any]")
    public ApiResponse<Order> getOrder(@PathVariable UUID id) {
        return ApiResponse.ok("Order retrieved", orderService.getOrder(id));
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders, paginated [ADMIN]")
    public ApiResponse<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Orders retrieved",
                orderService.getAllOrders(
                        PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PutMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status [ADMIN]")
    public ApiResponse<Order> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.ok("Order status updated", orderService.updateStatus(id, request));
    }
}
