package com.hashed.ecombend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a customer tries to order more units than are in stock.
 * Maps to HTTP 409 Conflict.
 * Because this extends RuntimeException, Spring will automatically roll back
 * any @Transactional method that lets this propagate — no partial orders.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format(
                "Insufficient stock for '%s': requested %d but only %d available",
                productName, requested, available
        ));
    }
}
