package com.hashed.ecombend.feature.catalog.product.dto;

import com.hashed.ecombend.feature.catalog.product.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for product endpoints.
 */
@Data
public class ProductResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private boolean onSale;
    private int stock;
    private boolean lowStock;
    private boolean active;
    private UUID categoryId;
    private String categoryName;
    private String primaryImageUrl;
    private List<ImageDto> images;

    @Data
    public static class ImageDto {
        private UUID id;
        private String url;
        private String altText;
        private boolean primary;
        private int sortOrder;
    }

    /**
     * Converts a Product entity to this response DTO.
     *
     * @param p The product entity
     * @return Populated ProductResponse
     */
    public static ProductResponse from(Product p) {
        ProductResponse dto = new ProductResponse();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setDescription(p.getDescription());
        dto.setSku(p.getSku());
        dto.setPrice(p.getPrice());
        dto.setCompareAtPrice(p.getCompareAtPrice());
        dto.setOnSale(p.isOnSale());
        dto.setStock(p.getStock());
        dto.setLowStock(p.isLowStock());
        dto.setActive(p.isActive());
        dto.setPrimaryImageUrl(p.getPrimaryImageUrl());

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }

        if (p.getImages() != null) {
            dto.setImages(p.getImages().stream().map(img -> {
                ImageDto i = new ImageDto();
                i.setId(img.getId());
                i.setUrl(img.getUrl());
                i.setAltText(img.getAltText());
                i.setPrimary(img.isPrimary());
                i.setSortOrder(img.getSortOrder());
                return i;
            }).toList());
        }

        return dto;
    }
}
