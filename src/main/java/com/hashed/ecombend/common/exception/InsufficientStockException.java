package com.hashed.ecombend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a customer attempts to order more units than are in stock.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    /**
     * @param productName The name of the product that is out of stock
     * @param requested   How many units were requested
     * @param available   How many units are available
     */
    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format(
                "Insufficient stock for '%s': requested %d but only %d available",
                productName, requested, available
        ));
    }
}