package com.mimimart.domain.review.model;

import java.time.LocalDateTime;

/**
 * 評價聚合根
 * 封裝評價業務邏輯與行為
 */
public class Review {
    private Long id;
    private final Long productId;
    private final Long memberId;
    private final Long orderItemId;
    private Rating rating;
    private String content;
    private String adminReply;
    private LocalDateTime adminRepliedAt;
    private boolean isVisible;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Review(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.memberId = builder.memberId;
        this.orderItemId = builder.orderItemId;
        this.rating = builder.rating;
        this.content = builder.content;
        this.adminReply = builder.adminReply;
        this.adminRepliedAt = builder.adminRepliedAt;
        this.isVisible = builder.isVisible;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : LocalDateTime.now();
    }

    /**
     * 建立建構器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 更新評價內容
     *
     * @param newRating 新評分
     * @param newContent 新評價內容
     */
    public void update(Rating newRating, String newContent) {
        if (newRating != null) {
            this.rating = newRating;
        }
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 管理員回覆評價
     *
     * @param replyContent 回覆內容
     */
    public void reply(String replyContent) {
        if (replyContent == null || replyContent.isBlank()) {
            throw new IllegalArgumentException("回覆內容不可為空");
        }
        this.adminReply = replyContent;
        this.adminRepliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 隱藏評價
     */
    public void hide() {
        this.isVisible = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 顯示評價
     */
    public void show() {
        this.isVisible = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 檢查評價是否屬於指定會員
     *
     * @param memberId 會員ID
     * @return 是否屬於該會員
     */
    public boolean belongsTo(Long memberId) {
        return this.memberId.equals(memberId);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Rating getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public LocalDateTime getAdminRepliedAt() {
        return adminRepliedAt;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Builder 模式建構器
     */
    public static class Builder {
        private Long id;
        private Long productId;
        private Long memberId;
        private Long orderItemId;
        private Rating rating;
        private String content;
        private String adminReply;
        private LocalDateTime adminRepliedAt;
        private boolean isVisible = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder orderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }

        public Builder rating(Rating rating) {
            this.rating = rating;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder adminReply(String adminReply) {
            this.adminReply = adminReply;
            return this;
        }

        public Builder adminRepliedAt(LocalDateTime adminRepliedAt) {
            this.adminRepliedAt = adminRepliedAt;
            return this;
        }

        public Builder isVisible(boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Review build() {
            if (productId == null || memberId == null || orderItemId == null || rating == null) {
                throw new IllegalArgumentException("產品ID、會員ID、訂單項目ID和評分為必填");
            }
            return new Review(this);
        }
    }
}
