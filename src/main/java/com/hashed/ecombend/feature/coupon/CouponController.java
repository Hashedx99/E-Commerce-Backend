package com.hashed.ecombend.feature.coupon;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.coupon.dto.CouponRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin only coupon management.
 * All endpoints require ADMIN role @PreAuthorize enforces this.
 */
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Coupons (Admin)", description = "Coupon creation and management")
public class CouponController {

    private final CouponAdminService couponAdminService;

    @GetMapping
    @Operation(summary = "List all coupons [ADMIN]")
    public ApiResponse<List<Coupon>> getAll() {
        return ApiResponse.ok("Coupons retrieved", couponAdminService.getAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a coupon [ADMIN]")
    public ApiResponse<Coupon> create(@Valid @RequestBody CouponRequest request) {
        return ApiResponse.ok("Coupon created", couponAdminService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a coupon [ADMIN]")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        couponAdminService.delete(id);
        return ApiResponse.ok("Coupon deleted");
    }
}
