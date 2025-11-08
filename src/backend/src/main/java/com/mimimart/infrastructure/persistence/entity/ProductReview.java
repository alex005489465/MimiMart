package com.mimimart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 商品評價實體
 * 對應資料表: product_reviews
 */
@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品ID（無外鍵約束，應用層驗證）
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 會員ID（無外鍵約束，應用層驗證）
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 訂單項目ID（無外鍵約束，應用層驗證唯一性）
     */
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    /**
     * 評分（1-5星，應用層驗證範圍）
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * 評價內容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 管理員回覆內容
     */
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    /**
     * 管理員回覆時間
     */
    @Column(name = "admin_replied_at")
    private LocalDateTime adminRepliedAt;

    /**
     * 顯示狀態（true=顯示, false=隱藏）
     */
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

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

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
