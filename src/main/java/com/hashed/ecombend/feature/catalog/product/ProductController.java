package com.hashed.ecombend.feature.catalog.product;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.catalog.product.dto.ProductRequest;
import com.hashed.ecombend.feature.catalog.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * GET endpoints are public per SecurityConfiguration.
 * POST / PUT / DELETE require ADMIN role.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;

    /**
     * Returns a paginated, filterable list of active products.
     * Supports optional filters: categoryId, minPrice, maxPrice.
     * Supports pagination: page, size, sortBy, direction.
     */
    @GetMapping
    @Operation(summary = "Get all products (paginated, filterable)")
    public ApiResponse<Page<ProductResponse>> getAll(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.ok("Products retrieved",
                productService.getAll(categoryId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ApiResponse<ProductResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok("Product retrieved", productService.getById(id));
