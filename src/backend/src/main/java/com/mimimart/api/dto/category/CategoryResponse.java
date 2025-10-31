package com.mimimart.api.dto.category;

import com.mimimart.infrastructure.persistence.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分類回應 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    /**
     * 從 Entity 轉換
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getSortOrder(),
            category.getCreatedAt()
        );
    }
}
