package com.hashed.ecombend.feature.catalog.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

/**
 * Request body for PUT /api/products/{id}. All fields are optional.
 */
@Data
public class ProductUpdateRequest {

	@Size(max = 200, message = "Name must be 200 characters or fewer")
	private String name;

	private String description;

	@DecimalMin(value = "0.01", message = "Price must be greater than zero")
	private BigDecimal price;

	private BigDecimal compareAtPrice;

	@Min(value = 0, message = "Stock cannot be negative")
	private Integer stock;

	@Min(value = 0, message = "Low stock threshold cannot be negative")
	private Integer lowStockThreshold;

	private UUID categoryId;
}

