package com.mimimart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品實體
 * 對應資料表: products
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品名稱
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 商品描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 商品售價
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 商品圖片 URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 分類 ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 啟用狀態 (true=啟用, false=停用)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 上架狀態 (true=已上架, false=未上架)
     */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /**
     * 上架時間 (NULL 表示不限制)
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 下架時間 (NULL 表示不限制)
     */
    @Column(name = "unpublished_at")
    private LocalDateTime unpublishedAt;

    /**
     * 軟刪除標記 (true=已刪除, false=未刪除)
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

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
     * 建立時自動設定時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 更新時自動設定時間
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 檢查商品是否在上架期間內
     *
     * @return true 如果當前時間在上架期間內
     */
    public boolean isInPublishPeriod() {
        LocalDateTime now = LocalDateTime.now();

        // 檢查是否已到上架時間 (NULL 或時間已到)
        boolean afterPublish = (publishedAt == null || !now.isBefore(publishedAt));

        // 檢查是否未到下架時間 (NULL 或時間未到)
        boolean beforeUnpublish = (unpublishedAt == null || now.isBefore(unpublishedAt));

        return afterPublish && beforeUnpublish;
    }

    /**
     * 檢查商品是否可購買 (已啟用、已上架、未刪除且在上架期間內)
     */
    public boolean isAvailable() {
        return isActive && isPublished && !isDeleted && isInPublishPeriod();
    }

    /**
     * 上架商品
     */
    public void publish() {
        if (isDeleted) {
            throw new IllegalStateException("已刪除的商品無法上架");
        }
        this.isPublished = true;
    }

    /**
     * 下架商品
     */
    public void unpublish() {
        this.isPublished = false;
    }

    /**
     * 標記為已刪除 (自動下架)
     */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.isPublished = false;
    }

    /**
     * 啟用商品
     */
    public void activate() {
        if (isDeleted) {
            throw new IllegalStateException("已刪除的商品無法啟用");
        }
        this.isActive = true;
    }

    /**
     * 停用商品 (自動下架)
     */
    public void deactivate() {
        this.isActive = false;
        this.isPublished = false;  // 停用時自動下架
    }
}
