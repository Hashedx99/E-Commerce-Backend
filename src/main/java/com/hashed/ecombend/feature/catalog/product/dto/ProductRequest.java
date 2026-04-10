package com.hashed.ecombend.feature.catalog.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for POST /api/products and PUT /api/products/{id}
 */
@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Name must be 200 characters or fewer")
    private String name;

    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be 50 characters or fewer")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    /**
     * Optional — must be greater than price if provided. Validated in service layer.
     */
    private BigDecimal compareAtPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock = 0;

    private int lowStockThreshold = 5;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
