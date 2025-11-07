package com.mimimart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 輪播圖 JPA 實體
 */
@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 輪播圖標題
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * S3 圖片 URL
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 點擊跳轉連結 (可為 null)
     */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /**
     * 顯示順序 (數字越小越優先顯示)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * 狀態: ACTIVE(啟用) / INACTIVE(停用)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerStatus status;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 上架時間 (NULL 表示立即上架)
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 下架時間 (NULL 表示永不下架)
     */
    @Column(name = "unpublished_at")
    private LocalDateTime unpublishedAt;

    /**
     * 自動設定建立時間
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 自動更新更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 業務方法 =====

    /**
     * 啟用輪播圖
     */
    public void activate() {
        this.status = BannerStatus.ACTIVE;
    }

    /**
     * 停用輪播圖
     */
    public void deactivate() {
        this.status = BannerStatus.INACTIVE;
    }

    /**
     * 更新顯示順序
     */
    public void updateOrder(Integer newOrder) {
        if (newOrder < 0) {
            throw new IllegalArgumentException("顯示順序不能為負數");
        }
        this.displayOrder = newOrder;
    }

    /**
     * 更新輪播圖資訊 (不含圖片)
     */
    public void updateInfo(String title, String linkUrl, Integer displayOrder) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        this.linkUrl = linkUrl; // 允許設為 null
        if (displayOrder != null) {
            updateOrder(displayOrder);
        }
    }

    /**
     * 更新圖片 URL
     */
    public void updateImageUrl(String newImageUrl) {
        if (newImageUrl == null || newImageUrl.isBlank()) {
            throw new IllegalArgumentException("圖片 URL 不能為空");
        }
        this.imageUrl = newImageUrl;
    }

    /**
     * 檢查輪播圖是否啟用
     */
    public boolean isActive() {
        return this.status == BannerStatus.ACTIVE;
    }

    /**
     * 檢查輪播圖是否在上架期間內
     * 判斷邏輯：
     * 1. 若 publishedAt 為 NULL，表示立即上架，檢查通過
     * 2. 若 publishedAt 不為 NULL，檢查當前時間是否 >= publishedAt
     * 3. 若 unpublishedAt 為 NULL，表示永不下架，檢查通過
     * 4. 若 unpublishedAt 不為 NULL，檢查當前時間是否 < unpublishedAt
     */
    public boolean isPublished() {
        LocalDateTime now = LocalDateTime.now();

        // 檢查是否已到上架時間
        if (publishedAt != null && now.isBefore(publishedAt)) {
            return false;
        }

        // 檢查是否已超過下架時間
        if (unpublishedAt != null && now.isAfter(unpublishedAt)) {
            return false;
        }

        return true;
    }
}
