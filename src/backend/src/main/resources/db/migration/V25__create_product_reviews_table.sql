-- V25: 建立商品評價表
-- 符合零約束設計原則: 無外鍵、無唯一索引、無 CHECK 約束
-- 所有驗證與約束都在應用層實作

CREATE TABLE product_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 外鍵欄位（無實際約束，應用層驗證）
    product_id BIGINT NOT NULL COMMENT '商品ID (應用層驗證存在性)',
    member_id BIGINT NOT NULL COMMENT '會員ID (應用層驗證存在性)',
    order_item_id BIGINT NOT NULL COMMENT '訂單項目ID (應用層驗證存在性+唯一性)',

    -- 評價內容（範圍驗證在應用層）
    rating INT NOT NULL COMMENT '評分 (應用層驗證1-5範圍)',
    content TEXT COMMENT '評價內容',

    -- 管理員回覆
    admin_reply TEXT COMMENT '管理員回覆內容',
    admin_replied_at TIMESTAMP NULL COMMENT '管理員回覆時間',

    -- 狀態與時間戳記
    is_visible BOOLEAN NOT NULL DEFAULT TRUE COMMENT '顯示狀態 (後台可隱藏)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    -- 普通索引（提升查詢效能）
    INDEX idx_product_id (product_id),
    INDEX idx_member_id (member_id),
    INDEX idx_order_item_id (order_item_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_product_visible (product_id, is_visible, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品評價表';
