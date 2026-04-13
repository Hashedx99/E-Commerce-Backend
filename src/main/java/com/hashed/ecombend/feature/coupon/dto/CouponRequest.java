package com.hashed.ecombend.feature.coupon.dto;

import com.hashed.ecombend.feature.coupon.CouponType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Code must be 50 characters or fewer")
    private String code;

    @NotNull(message = "Coupon type is required")
    private CouponType type;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.01", message = "Value must be greater than zero")
    private BigDecimal value;

    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount;

    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    private LocalDateTime expiresAt;
}
