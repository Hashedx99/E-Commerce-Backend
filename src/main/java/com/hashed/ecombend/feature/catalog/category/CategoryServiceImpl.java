package com.hashed.ecombend.feature.catalog.category;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SlugUtil;
import com.hashed.ecombend.feature.catalog.category.dto.CategoryRequest;
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
    public Category create(CategoryRequest request) {
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
    public Category update(UUID id, CategoryRequest request) {
