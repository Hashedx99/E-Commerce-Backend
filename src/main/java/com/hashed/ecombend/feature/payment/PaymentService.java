package com.hashed.ecombend.feature.payment;

import com.hashed.ecombend.feature.order.Order;

public interface PaymentService {

    /**
     * Creates and processes a payment for the given order.
     * Called by OrderServiceImpl immediately after saving the order.
     *
     * @param order The just-placed order to pay for
     * @return The persisted Payment record
     */
    Payment createPayment(Order order);

    /**
     * Refunds a completed payment.
     * Called when an order transitions to REFUNDED status.
     *
     * @param payment A COMPLETED payment to refund
     * @return The updated Payment with status REFUNDED
     */
    Payment refund(Payment payment);
}
