package com.hashed.ecombend.feature.coupon;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.feature.coupon.dto.CouponRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponAdminServiceImpl implements CouponAdminService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAll() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon create(CouponRequest request) {
        validateRequest(request);

        // Enforce code uniqueness
        couponRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(c -> {
            throw new BusinessException(
                    "A coupon with code '" + request.getCode() + "' already exists");
        });

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setType(request.getType());
        coupon.setValue(request.getValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setActive(true);

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created: {}", saved.getCode());
        return saved;
    }

    @Override
    public void delete(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        couponRepository.delete(coupon);
        log.info("Coupon deleted: {}", coupon.getCode());
    }

    private void validateRequest(CouponRequest request) {
        if (request.getMinOrderAmount() != null
                && request.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Minimum order amount cannot be negative");
        }

        if (request.getType() == CouponType.PERCENTAGE
                && request.getValue() != null
                && request.getValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Percentage coupon value cannot exceed 100");
        }
    }
}
