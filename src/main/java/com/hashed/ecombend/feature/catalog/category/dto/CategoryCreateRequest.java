package com.hashed.ecombend.feature.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

/**
 * Request body for POST /api/categories.
 */
@Data
public class CategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    private String imageUrl;

    private UUID parentId;

    private Integer sortOrder;
}

