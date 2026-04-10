package com.hashed.ecombend.feature.catalog.category;

import com.hashed.ecombend.feature.catalog.category.dto.CategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    /**
     * returns all active, non-deleted categories.
     */
    List<Category> getAll();

    /**
     * returns a single category by id.
     */
    Category getById(UUID id);

    /**
     * creates a new category. Admin only.
     */
    Category create(CategoryRequest request);

    /**
     * updates an existing category. Admin only.
     */
    Category update(UUID id, CategoryRequest request);

    /**
     * soft deletes a category. Admin only.
     */
    void delete(UUID id);
}
