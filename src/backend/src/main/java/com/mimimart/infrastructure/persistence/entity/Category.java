package com.mimimart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 商品分類實體
 * 對應資料表: categories
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分類名稱
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 分類描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 排序權重 (數字越小越前面)
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 軟刪除時間戳 (NULL=未刪除)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 建立時自動設定時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 檢查是否已刪除
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 標記為已刪除
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
