package com.mimimart.api.dto.product;

import com.mimimart.infrastructure.persistence.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品詳情回應 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Long categoryId;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime unpublishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 評價統計：平均評分（1-5星）
     */
    private Double avgRating;

    /**
     * 評價統計：總評價數
     */
    private Long totalReviews;

    /**
     * 評價統計：評分分布
     * key: 評分（1-5）
     * value: 該評分的數量
     */
    private Map<Integer, Long> ratingDistribution;

    /**
     * 從 Entity 轉換（無評價統計）
     */
    public static ProductDetailResponse from(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());
        response.setCategoryId(product.getCategoryId());
        response.setIsPublished(product.getIsPublished());
        response.setPublishedAt(product.getPublishedAt());
        response.setUnpublishedAt(product.getUnpublishedAt());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    /**
     * 從 Entity 轉換（含評價統計）
     */
    public static ProductDetailResponse fromWithReviewStats(
            Product product,
            Double avgRating,
            Long totalReviews,
            Map<Integer, Long> ratingDistribution
    ) {
        ProductDetailResponse response = from(product);
        response.setAvgRating(avgRating);
        response.setTotalReviews(totalReviews);
        response.setRatingDistribution(ratingDistribution);
        return response;
    }
}
