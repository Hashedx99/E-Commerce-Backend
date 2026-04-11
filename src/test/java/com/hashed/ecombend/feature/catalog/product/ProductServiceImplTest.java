package com.hashed.ecombend.feature.catalog.product;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.DuplicateResourceException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.feature.catalog.category.Category;
import com.hashed.ecombend.feature.catalog.category.CategoryRepository;
import com.hashed.ecombend.feature.catalog.product.dto.ProductRequest;
import com.hashed.ecombend.feature.catalog.product.dto.ProductResponse;
import com.hashed.ecombend.feature.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository imageRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("create: success — saves product with generated slug")
    void create_success() {
        Category category = buildCategory();
        ProductRequest req = buildRequest("Apple Watch Series 9", "ELEC-AW-001", new BigDecimal("399.99"), null);

        when(categoryRepository.findById(req.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.existsBySku("ELEC-AW-001")).thenReturn(false);
        when(productRepository.findBySlug(any())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            return p;
        });

        ProductResponse result = productService.create(req);

        assertThat(result.getName()).isEqualTo("Apple Watch Series 9");
        assertThat(result.getSlug()).isEqualTo("apple-watch-series-9");
        assertThat(result.getSku()).isEqualTo("ELEC-AW-001");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("399.99"));
    }

    @Test
    @DisplayName("create: category not found — throws ResourceNotFoundException")
    void create_categoryNotFound_throwsException() {
        ProductRequest req = buildRequest("Watch", "SKU-001", new BigDecimal("99.99"), null);
        when(categoryRepository.findById(req.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(req)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Category");
    }

    @Test
    @DisplayName("create: duplicate SKU — throws DuplicateResourceException")
    void create_duplicateSku_throwsException() {
        Category category = buildCategory();
        ProductRequest req = buildRequest("Watch", "ELEC-AW-001", new BigDecimal("99.99"), null);

        when(categoryRepository.findById(req.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.existsBySku("ELEC-AW-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(req)).isInstanceOf(DuplicateResourceException.class).hasMessageContaining("SKU");
    }

    @Test
    @DisplayName("create: compareAtPrice <= price — throws BusinessException")
    void create_invalidCompareAtPrice_throwsException() {
        Category category = buildCategory();
        ProductRequest req = buildRequest("Watch", "SKU-002", new BigDecimal("99.99"), new BigDecimal("50.00")); //
        // compareAt < price — invalid

        when(categoryRepository.findById(req.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.existsBySku(any())).thenReturn(false);

        assertThatThrownBy(() -> productService.create(req)).isInstanceOf(BusinessException.class).hasMessageContaining("Compare-at price");
    }

    @Test
    @DisplayName("delete: success — soft-deletes the product")
    void delete_success() {
        Product product = new Product();
        product.setName("Watch");
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.delete(id);

        assertThat(product.isDeleted()).isTrue();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("delete: product not found — throws ResourceNotFoundException")
    void delete_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Product");
    }

    @Test
    @DisplayName("uploadImages: exceeds max — throws BusinessException")
    void uploadImages_tooManyImages_throwsException() {
        UUID id = UUID.randomUUID();
        Product product = new Product();
        product.setName("Watch");

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(imageRepository.countByProductId(id)).thenReturn(6);

        // Trying to add 3 more when 6 already exist (total 9 > max 8)
        var files = java.util.Collections.nCopies(3, mock(org.springframework.web.multipart.MultipartFile.class));

        assertThatThrownBy(() -> productService.uploadImages(id, files)).isInstanceOf(BusinessException.class).hasMessageContaining("8 images");
    }

    private Category buildCategory() {
        Category c = new Category();
        c.setName("Electronics");
        c.setSlug("electronics");
        // Set id via reflection since UUID is set by JPA in real usage
        try {
            var f = com.hashed.ecombend.common.entity.BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(c, UUID.randomUUID());
        } catch (Exception ignored) {
        }
        return c;
    }

    private ProductRequest buildRequest(String name, String sku, BigDecimal price, BigDecimal compareAtPrice) {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setSku(sku);
        req.setPrice(price);
        req.setCompareAtPrice(compareAtPrice);
        req.setCategoryId(UUID.randomUUID());
        req.setStock(10);
        req.setLowStockThreshold(5);
        return req;
    }
}
