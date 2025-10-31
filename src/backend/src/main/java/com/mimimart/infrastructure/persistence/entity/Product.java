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
     * 售價/促銷價
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 原價 (用於顯示折扣)
     */
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

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
     * 上架狀態 (true=已上架, false=未上架)
     */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

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
     * 檢查商品是否可購買 (已上架且未刪除)
     */
    public boolean isAvailable() {
        return isPublished && !isDeleted;
    }

    /**
     * 檢查是否有折扣
     */
    public boolean hasDiscount() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
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
}
