package com.hashed.ecombend.feature.catalog.product;

import com.hashed.ecombend.feature.catalog.product.dto.ProductCreateRequest;
import com.hashed.ecombend.feature.catalog.product.dto.ProductResponse;
import com.hashed.ecombend.feature.catalog.product.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    /**
     * paginated, filterable product list.
     */
    Page<ProductResponse> getAll(UUID categoryId, BigDecimal minPrice,
                                 BigDecimal maxPrice, Pageable pageable);

    /**
     * single product detail with all images.
     */
    ProductResponse getById(UUID id);

    /**
     * creates a product. Admin only.
     */
    ProductResponse create(ProductCreateRequest request);

    /**
     * updates a product. Admin only.
     */
    ProductResponse update(UUID id, ProductUpdateRequest request);

    /**
     * soft-deletes a product. Admin only.
     */
    void delete(UUID id);

    /**
     * uploads images for a product. Admin only.
     */
    List<ProductImage> uploadImages(UUID productId, List<MultipartFile> files);
}
