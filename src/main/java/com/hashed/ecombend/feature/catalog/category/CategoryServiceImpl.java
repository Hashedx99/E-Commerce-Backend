package com.hashed.ecombend.feature.catalog.category;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SlugUtil;
import com.hashed.ecombend.feature.catalog.category.dto.CategoryCreateRequest;
import com.hashed.ecombend.feature.catalog.category.dto.CategoryUpdateRequest;
import com.hashed.ecombend.feature.catalog.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * category CRUD.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    /**
     * Creates a new category. Generates a slug from the name and resolves
     * the parent category if parentId is provided.
     * Slug uniqueness is enforced by the @Index unique constraint on the slug column.
     * If a duplicate slug reaches the DB, DataIntegrityViolationException fires and
     * RestExceptionHandler returns 409.
     *
     * @throws ResourceNotFoundException if parentId doesn't exist
     */
    @Override
    public Category create(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(generateUniqueSlug(request.getName(), null));
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setActive(true);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category", "id", request.getParentId()));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} (slug: {})", saved.getName(), saved.getSlug());
        return saved;
    }


    /**
     * Updates an existing category. Regenerates the slug if the name changed.
     *
     * @throws ResourceNotFoundException if category not found
     */
    @Override
    public Category update(UUID id, CategoryUpdateRequest request) {
        Category category = getById(id);

        // Regenerate slug only if name actually changed
        if (StringUtils.hasText(request.getName())
                && !request.getName().equalsIgnoreCase(category.getName())) {
            category.setName(request.getName());
            category.setSlug(generateUniqueSlug(request.getName(), id));
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getImageUrl() != null) category.setImageUrl(request.getImageUrl());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category", "id", request.getParentId()));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category updated: {}", saved.getName());
        return saved;
    }

    /**
     * Soft deletes a category. Guards against deleting a category that still has
     * active products or child categories referencing it.
     *
     * @throws BusinessException         if active products or sub-categories exist
     * @throws ResourceNotFoundException if category not found
     */
    @Override
    public void delete(UUID id) {
        Category category = getById(id);

        if (productRepository.existsByCategoryIdAndDeletedAtIsNull(id)) {
            throw new BusinessException(
                    "Cannot delete category '" + category.getName()
                            + "' — it still has active products. Remove or reassign them first.");
        }

        if (categoryRepository.existsByParentId(id)) {
            throw new BusinessException(
                    "Cannot delete category '" + category.getName()
                            + "' — it has sub-categories. Delete or reassign them first.");
        }

        category.softDelete();
        categoryRepository.save(category);
        log.info("Category soft-deleted: {}", category.getName());
    }

    /**
     * Generates a slug from the name, appending a numeric suffix if the base
     * slug is already taken by a different category.
     *
     * @param name      The category name
     * @param excludeId The id of the current category to exclude from uniqueness check
     *                  (null for new categories)
     * @return A unique slug string
     */
    private String generateUniqueSlug(String name, UUID excludeId) {
        String base = SlugUtil.generate(name);
        String candidate = base;
        int suffix = 2;

        while (true) {
            final String slug = candidate;
            boolean taken = categoryRepository.findBySlug(slug)
                    .filter(c -> !c.getId().equals(excludeId))
                    .isPresent();
            if (!taken) return slug;
            candidate = SlugUtil.withSuffix(base, suffix++);
        }
    }
}
