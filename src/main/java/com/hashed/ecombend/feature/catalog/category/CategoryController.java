package com.hashed.ecombend.feature.catalog.category;

import com.hashed.ecombend.common.response.ApiResponse;
import com.hashed.ecombend.feature.catalog.category.dto.CategoryCreateRequest;
import com.hashed.ecombend.feature.catalog.category.dto.CategoryUpdateRequest;
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
 * READ endpoints are public — no JWT needed (see SecurityConfiguration).
 * WRITE endpoints require ADMIN role via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management")
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping
    @Operation(summary = "Get all categories")
    public ApiResponse<List<Category>> getAll() {
        return ApiResponse.ok("Categories retrieved", categoryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ApiResponse<Category> getById(@PathVariable UUID id) {
        return ApiResponse.ok("Category retrieved", categoryService.getById(id));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new category [ADMIN]")
    public ApiResponse<Category> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.ok("Category created", categoryService.create(request));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a category [ADMIN]")
    public ApiResponse<Category> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ApiResponse.ok("Category updated", categoryService.update(id, request));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a category [ADMIN]")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ApiResponse.ok("Category deleted");
    }
}
