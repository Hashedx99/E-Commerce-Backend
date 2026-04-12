package com.hashed.ecombend.feature.payment;

/**
 * The external payment gateway that processed this payment.
 */
public enum PaymentProvider {
    MANUAL,
    STRIPE,
    PAYPAL,
    COD
}
