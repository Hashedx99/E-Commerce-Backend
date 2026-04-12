package com.hashed.ecombend.feature.coupon;

import java.math.BigDecimal;

/**
 * CouponServiceImpl is currently a stub that returns zero discount.
 */
public interface CouponService {

    /**
     * Validates a coupon code against the given subtotal.
     * Returns the Coupon if valid. Throws BusinessException if invalid.
     *
     * @param code     The coupon code submitted by the customer
     * @param subtotal The order subtotal before discount
     * @return The validated Coupon entity
     */
    Coupon validate(String code, BigDecimal subtotal);

    /**
     * Calculates the discount amount to subtract from the subtotal.
     *
     * @param coupon   A validated coupon (output of validate())
     * @param subtotal The order subtotal before discount
     * @return The discount amount
     */
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal);

    /**
     * Increments the usage counter on the coupon after a successful order.
     * Call this only after the order is committed.
     *
     * @param coupon The coupon that was applied
     */
    void incrementUsage(Coupon coupon);
}
