package com.mimimart.api.dto.product;

import com.mimimart.infrastructure.persistence.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品回應 DTO (列表用)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Long categoryId;
    private Boolean isPublished;
    private LocalDateTime createdAt;

    /**
     * 從 Entity 轉換
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl(),
            product.getCategoryId(),
            product.getIsPublished(),
            product.getCreatedAt()
        );
    }
}
