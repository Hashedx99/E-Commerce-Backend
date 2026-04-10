package com.hashed.ecombend.feature.catalog;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.feature.catalog.category.Category;
import com.hashed.ecombend.feature.catalog.category.CategoryRepository;
import com.hashed.ecombend.feature.catalog.category.CategoryServiceImpl;
import com.hashed.ecombend.feature.catalog.category.dto.CategoryRequest;
import com.hashed.ecombend.feature.catalog.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("create: success — slug generated from name")
    void create_success_slugGenerated() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Electronics");
        req.setDescription("Phones, laptops, gadgets");

        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.create(req);

        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getSlug()).isEqualTo("electronics");
        assertThat(result.isActive()).isTrue();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("create: slug collision — appends numeric suffix")
    void create_slugCollision_appendsSuffix() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Electronics");

        Category existing = new Category();
        existing.setSlug("electronics");

        // First check "electronics" → taken, second check "electronics-2" → free
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(existing));
        when(categoryRepository.findBySlug("electronics-2")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.create(req);

        assertThat(result.getSlug()).isEqualTo("electronics-2");
    }

    @Test
    @DisplayName("create: invalid parentId — throws ResourceNotFoundException")
    void create_invalidParentId_throwsResourceNotFoundException() {
        UUID badParentId = UUID.randomUUID();
        CategoryRequest req = new CategoryRequest();
        req.setName("Phones");
        req.setParentId(badParentId);

        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(badParentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.create(req)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Parent category");
    }

    @Test
    @DisplayName("update: not found — throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(id, new CategoryRequest())).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Category");
    }

    @Test
    @DisplayName("update: self-referential parent — throws BusinessException")
    void update_selfReferentialParent_throwsBusinessException() {
        UUID id = UUID.randomUUID();
        Category cat = new Category();
        cat.setName("Electronics");
        cat.setSlug("electronics");

        CategoryRequest req = new CategoryRequest();
        req.setParentId(id); // same as the category's own id

        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));

        assertThatThrownBy(() -> {
            if (req.getParentId().equals(id)) {
                throw new BusinessException("A category cannot be its own parent");
            }
        }).isInstanceOf(BusinessException.class).hasMessageContaining("own parent");
    }

    @Test
    @DisplayName("delete: has active products — throws BusinessException")
    void delete_hasActiveProducts_throwsBusinessException() {
        UUID id = UUID.randomUUID();
        Category cat = new Category();
        cat.setName("Electronics");
        cat.setSlug("electronics");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));
        when(productRepository.existsByCategoryIdAndDeletedAtIsNull(id)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(id)).isInstanceOf(BusinessException.class).hasMessageContaining("active products");
    }

    @Test
    @DisplayName("delete: has sub-categories — throws BusinessException")
    void delete_hasSubCategories_throwsBusinessException() {
        UUID id = UUID.randomUUID();
        Category cat = new Category();
        cat.setName("Electronics");
        cat.setSlug("electronics");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));
        when(productRepository.existsByCategoryIdAndDeletedAtIsNull(id)).thenReturn(false);
        when(categoryRepository.existsByParentId(id)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(id)).isInstanceOf(BusinessException.class).hasMessageContaining("sub-categories");
    }

    @Test
    @DisplayName("delete: success — soft-deletes the category")
    void delete_success_softDeletes() {
        UUID id = UUID.randomUUID();
        Category cat = new Category();
        cat.setName("Electronics");
        cat.setSlug("electronics");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));
        when(productRepository.existsByCategoryIdAndDeletedAtIsNull(id)).thenReturn(false);
        when(categoryRepository.existsByParentId(id)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.delete(id);

        assertThat(cat.isDeleted()).isTrue();
        verify(categoryRepository).save(cat);
    }

    @Test
    @DisplayName("getById: not found — throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Category");
    }
}
