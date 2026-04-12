package com.hashed.ecombend.feature.coupon;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Coupon service implementation.
 * Currently validates basic constraints (exists, active, not expired, usage limit, minimum order) and calculates
 * discounts.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    /**
     * Validates a coupon code. Checks:
     * 1. Code exists
     * 2. Coupon is active
     * 3. Coupon has not expired
     * 4. Usage limit not reached
     * 5. Order meets minimum amount
     *
     * @throws ResourceNotFoundException if code not found
     * @throws BusinessException         if any validation fails
     */
    @Override
    @Transactional(readOnly = true)
    public Coupon validate(String code, BigDecimal subtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException(
                        "Coupon code '" + code + "' is not valid"));

        if (!coupon.isActive()) {
            throw new BusinessException("Coupon '" + code + "' is no longer active");
        }

        if (coupon.getExpiresAt() != null
                && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Coupon '" + code + "' has expired");
        }

        if (coupon.getMaxUses() != null
                && coupon.getUsesCount() >= coupon.getMaxUses()) {
            throw new BusinessException("Coupon '" + code + "' has reached its usage limit");
        }

        if (coupon.getMinOrderAmount() != null
                && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException(
                    "Minimum order of $" + coupon.getMinOrderAmount()
                            + " required for this coupon");
        }

        return coupon;
    }

    /**
     * Calculates the discount amount.
     * PERCENTAGE: discount = subtotal × (value / 100), capped at subtotal.
     * FIXED_AMOUNT: discount = value, capped at subtotal.
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount = switch (coupon.getType()) {
            case PERCENTAGE -> subtotal
                    .multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> coupon.getValue();
        };

        // Discount can never exceed the subtotal
        return discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Increments usage counter after a successful order.
     */
    @Override
    public void incrementUsage(Coupon coupon) {
        coupon.setUsesCount(coupon.getUsesCount() + 1);
        couponRepository.save(coupon);
        log.info("Coupon '{}' used — total uses: {}", coupon.getCode(), coupon.getUsesCount());
    }
}
