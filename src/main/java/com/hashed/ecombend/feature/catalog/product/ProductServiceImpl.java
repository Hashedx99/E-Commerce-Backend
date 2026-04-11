package com.hashed.ecombend.feature.catalog.product;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.DuplicateResourceException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SlugUtil;
import com.hashed.ecombend.feature.catalog.category.Category;
import com.hashed.ecombend.feature.catalog.category.CategoryRepository;
import com.hashed.ecombend.feature.catalog.product.dto.ProductRequest;
import com.hashed.ecombend.feature.catalog.product.dto.ProductResponse;
import com.hashed.ecombend.feature.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    private static final int MAX_IMAGES = 8;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(UUID categoryId, BigDecimal minPrice,
                                        BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findFiltered(categoryId, minPrice, maxPrice, pageable)
                .map(ProductResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductResponse.from(product);
    }

    /**
     * Creates a new product. Validates:
     * - Category exists
     * - SKU is unique
     * - compareAtPrice > price (if provided)
     * Generates a unique slug from the name via SlugUtil.
     */
    @Override
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "id", request.getCategoryId()));

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "SKU", request.getSku());
        }

        validateCompareAtPrice(request.getPrice(), request.getCompareAtPrice());

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(generateUniqueSlug(request.getName(), null));
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setStock(request.getStock());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setCategory(category);
        product.setActive(true);

        Product saved = productRepository.save(product);
        log.info("Product created: {} (SKU: {})", saved.getName(), saved.getSku());
        return ProductResponse.from(saved);
    }

    /**
