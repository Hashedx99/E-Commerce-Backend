package com.hashed.ecombend.common.exception;

/**
 * Raised when transactional email delivery fails.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

