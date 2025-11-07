package com.mimimart.api.dto.banner;

import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 輪播圖回應 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {

    /**
     * 輪播圖 ID
     */
    private Long id;

    /**
     * 輪播圖標題
     */
    private String title;

    /**
     * S3 圖片 URL
     */
    private String imageUrl;

    /**
     * 點擊跳轉連結
     */
    private String linkUrl;

    /**
     * 顯示順序
     */
    private Integer displayOrder;

    /**
     * 狀態
     */
    private BannerStatus status;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;

    /**
     * 上架時間 (NULL 表示立即上架)
     */
    private LocalDateTime publishedAt;

    /**
     * 下架時間 (NULL 表示永不下架)
     */
    private LocalDateTime unpublishedAt;

    /**
     * 從 Entity 轉換為 Response DTO
     */
    public static BannerResponse from(BannerEntity entity) {
        return new BannerResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getLinkUrl(),
                entity.getDisplayOrder(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPublishedAt(),
                entity.getUnpublishedAt()
        );
    }
}
