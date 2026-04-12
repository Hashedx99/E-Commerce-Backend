package com.hashed.ecombend.feature.coupon;

import com.hashed.ecombend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A discount coupon that can be applied at checkout.
 */
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CouponType type;

    /**
     * The discount value percentage (0–100) or fixed amount.
     */
    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    /**
     * Minimum order subtotal required to use this coupon. Null = no minimum.
     */
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    /**
     * Maximum number of times this coupon can be used. Null = unlimited.
     */
    @Column(name = "max_uses")
    private Integer maxUses;

    /**
     * How many times this coupon has been used so far.
     */
    @Column(name = "uses_count", nullable = false)
    private int usesCount = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
