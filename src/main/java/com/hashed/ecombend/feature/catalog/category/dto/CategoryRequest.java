package com.hashed.ecombend.feature.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Request body for create and update category endpoints.
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    private String imageUrl;

    /**
     * Null = top-level category. Must reference an existing category id if provided.
     */
    private UUID parentId;

    private Integer sortOrder;
}
