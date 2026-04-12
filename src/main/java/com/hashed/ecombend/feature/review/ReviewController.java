package com.hashed.ecombend.feature.review;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.review.dto.ReviewRequest;
import com.hashed.ecombend.feature.review.dto.ReviewResponse;
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


@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reviews", description = "Product review management")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    @Operation(summary = "Get all reviews for a product")
    public ApiResponse<List<ReviewResponse>> getForProduct(@PathVariable UUID productId) {
        return ApiResponse.ok("Reviews retrieved", reviewService.getForProduct(productId));
    }

    @PostMapping("/api/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a review for a product [CUSTOMER]")
    public ApiResponse<ReviewResponse> create(
            @PathVariable UUID productId,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok("Review created", reviewService.create(productId, request));
    }


    @PutMapping("/api/reviews/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Edit your review [CUSTOMER]")
    public ApiResponse<ReviewResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok("Review updated", reviewService.update(id, request));
    }

    @DeleteMapping("/api/reviews/{id}")
    @Operation(summary = "Delete a review [CUSTOMER own | ADMIN any]")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ApiResponse.ok("Review deleted");
    }
}
