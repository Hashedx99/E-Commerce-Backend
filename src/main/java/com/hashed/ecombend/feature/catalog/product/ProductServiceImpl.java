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
     * Updates a product. Regenerates slug only if the name changed.
     * SKU changes are not allowed create a new product instead.
     */
    @Override
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (StringUtils.hasText(request.getName())
                && !request.getName().equalsIgnoreCase(product.getName())) {
            product.setName(request.getName());
            product.setSlug(generateUniqueSlug(request.getName(), id));
        }

        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) {
            validateCompareAtPrice(request.getPrice(), request.getCompareAtPrice());
            product.setPrice(request.getPrice());
        }
        if (request.getCompareAtPrice() != null) product.setCompareAtPrice(request.getCompareAtPrice());
        product.setStock(request.getStock());
        product.setLowStockThreshold(request.getLowStockThreshold());

        if (request.getCategoryId() != null
                && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("Product updated: {}", saved.getName());
        return ProductResponse.from(saved);
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.softDelete();
        productRepository.save(product);
        log.info("Product soft-deleted: {}", product.getName());
    }

    /**
     * Uploads up to 8 images for a product. Files are stored via StorageService.
     * The first uploaded image is set as primary if no primary exists yet.
     *
     * @throws BusinessException if the product already has 8 images
     */
    @Override
    public List<ProductImage> uploadImages(UUID productId, List<MultipartFile> files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int existing = imageRepository.countByProductId(productId);
        if (existing + files.size() > MAX_IMAGES) {
            throw new BusinessException(
                    "A product can have at most " + MAX_IMAGES + " images. "
                            + "Currently has " + existing + ".");
        }

        boolean hasPrimary = imageRepository.existsByProductIdAndPrimaryTrue(productId);

        List<ProductImage> saved = files.stream().map(file -> {
            String url = storageService.store(
                    file, "products/" + productId);

            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl(url);
            img.setAltText(product.getName());
            img.setSortOrder(imageRepository.countByProductId(productId));

            // First image uploaded becomes primary if none exists
            if (!hasPrimary && files.indexOf(file) == 0) {
                img.setPrimary(true);
            }

            return imageRepository.save(img);
        }).toList();

        log.info("Uploaded {} image(s) for product: {}", saved.size(), product.getName());
        return saved;
    }

    private void validateCompareAtPrice(BigDecimal price, BigDecimal compareAtPrice) {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) <= 0) {
            throw new BusinessException(
                    "Compare-at price must be greater than the sale price");
        }
    }

    private String generateUniqueSlug(String name, UUID excludeId) {
        String base = SlugUtil.generate(name);
        String candidate = base;
        int suffix = 2;

        while (true) {
            final String slug = candidate;
            boolean taken = productRepository.findBySlug(slug)
                    .filter(p -> !p.getId().equals(excludeId))
                    .isPresent();
            if (!taken) return slug;
            candidate = SlugUtil.withSuffix(base, suffix++);
        }
    }
}
