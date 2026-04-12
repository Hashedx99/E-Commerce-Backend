package com.hashed.ecombend.feature.coupon;

import com.hashed.ecombend.feature.coupon.dto.CouponRequest;

import java.util.List;
import java.util.UUID;

/**
 * Admin-facing coupon management separate from CouponService
 * which handles customer facing validation and discount calculation.
 */
public interface CouponAdminService {
    List<Coupon> getAll();

    Coupon create(CouponRequest request);

    void delete(UUID id);
}
