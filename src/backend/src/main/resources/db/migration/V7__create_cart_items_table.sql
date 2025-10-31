-- 購物車項目表
-- 零約束設計，所有資料完整性由應用程式層驗證
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '購物車項目唯一識別碼',
    member_id BIGINT NOT NULL COMMENT '會員 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    quantity INT NOT NULL COMMENT '商品數量',
    -- snapshot_price DECIMAL(10, 2) NULL COMMENT 'TODO: 價格快照（預留給未來價格變動功能）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_member_id (member_id) COMMENT '會員查詢優化索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='購物車項目表';
