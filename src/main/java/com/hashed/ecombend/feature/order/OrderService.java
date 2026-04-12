package com.hashed.ecombend.feature.order;

import com.hashed.ecombend.feature.order.dto.PlaceOrderRequest;
import com.hashed.ecombend.feature.order.dto.UpdateOrderStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    /**
     * places an order with stock decrement and optimistic locking.
     */
    Order placeOrder(PlaceOrderRequest request);

    /**
     * returns the current user's order history.
     */
    List<Order> getMyOrders();

    /**
     * returns a specific order. Validates ownership unless ADMIN.
     */
    Order getOrder(UUID orderId);

    /**
     * returns all orders. Admin only.
     */
    Page<Order> getAllOrders(Pageable pageable);

    /**
     * updates order status. Admin only.
     */
    Order updateStatus(UUID orderId, UpdateOrderStatusRequest request);
}
