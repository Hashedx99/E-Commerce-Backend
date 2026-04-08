package com.hashed.ecombend.feature.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    /**
     * Used before deleting a category, cannot delete if products still reference it.
     */
    boolean existsByParentId(UUID parentId);
}
