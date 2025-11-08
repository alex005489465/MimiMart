-- 重構購物車:將 cart_items 改為 member_carts 主表
-- 購物車項目改用 Redis 儲存,此表僅記錄會員購物車元數據

-- 1. 刪除舊的 cart_items 表 (目前無資料,可直接刪除)
DROP TABLE IF EXISTS cart_items;

-- 2. 建立新的 member_carts 主表
CREATE TABLE member_carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '購物車唯一識別碼',
    member_id BIGINT NOT NULL COMMENT '會員 ID',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '購物車狀態 (ACTIVE/EXPIRED)',
    max_items_count INT DEFAULT 100 COMMENT '購物車項目上限',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_member_id (member_id) COMMENT '會員查詢索引',
    INDEX idx_status (status) COMMENT '狀態查詢索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員購物車主表(元數據)';
