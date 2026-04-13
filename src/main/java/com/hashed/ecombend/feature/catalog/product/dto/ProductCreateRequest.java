package com.hashed.ecombend.feature.catalog.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

/**
 * Request body for POST /api/products.
 */
@Data
public class ProductCreateRequest {

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

    private BigDecimal compareAtPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock = 0;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private int lowStockThreshold = 5;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}

