package com.mimimart.api.dto.product;

import com.mimimart.infrastructure.persistence.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品詳情回應 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Long categoryId;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime unpublishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 從 Entity 轉換
     */
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getImageUrl(),
            product.getCategoryId(),
            product.getIsPublished(),
            product.getPublishedAt(),
            product.getUnpublishedAt(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
