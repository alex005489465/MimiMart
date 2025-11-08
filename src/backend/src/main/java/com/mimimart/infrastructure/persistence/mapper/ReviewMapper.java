package com.mimimart.infrastructure.persistence.mapper;

import com.mimimart.domain.review.model.Rating;
import com.mimimart.domain.review.model.Review;
import com.mimimart.infrastructure.persistence.entity.ProductReview;
import org.springframework.stereotype.Component;

/**
 * 評價 Mapper
 * 負責領域模型 ↔ JPA 實體的雙向轉換
 */
@Component
public class ReviewMapper {

    /**
     * 領域模型 → JPA 實體
     *
     * @param review 評價領域模型
     * @return ProductReview JPA 實體
     */
    public ProductReview toEntity(Review review) {
        ProductReview entity = new ProductReview();
        entity.setId(review.getId());
        entity.setProductId(review.getProductId());
        entity.setMemberId(review.getMemberId());
        entity.setOrderItemId(review.getOrderItemId());
        entity.setRating(review.getRating().getValue());
        entity.setContent(review.getContent());
        entity.setAdminReply(review.getAdminReply());
        entity.setAdminRepliedAt(review.getAdminRepliedAt());
        entity.setIsVisible(review.isVisible());
        entity.setCreatedAt(review.getCreatedAt());
        entity.setUpdatedAt(review.getUpdatedAt());
        return entity;
    }

    /**
     * JPA 實體 → 領域模型
     *
     * @param entity ProductReview JPA 實體
     * @return Review 領域模型
     */
    public Review toDomain(ProductReview entity) {
        return Review.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .memberId(entity.getMemberId())
                .orderItemId(entity.getOrderItemId())
                .rating(Rating.of(entity.getRating()))
                .content(entity.getContent())
                .adminReply(entity.getAdminReply())
                .adminRepliedAt(entity.getAdminRepliedAt())
                .isVisible(entity.getIsVisible())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
