-- V6: 建立商品資料表
-- 設計: 零約束設計 (無外鍵、UNIQUE、CHECK 約束,應用程式層負責)

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '商品名稱',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '售價/促銷價',
    original_price DECIMAL(10, 2) COMMENT '原價 (可選,用於顯示折扣)',
    image_url VARCHAR(500) COMMENT '商品圖片 URL',
    category_id BIGINT NOT NULL COMMENT '分類 ID (無外鍵約束)',
    is_published TINYINT(1) NOT NULL DEFAULT 1 COMMENT '上架狀態 (1=已上架, 0=未上架)',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '軟刪除標記 (1=已刪除, 0=未刪除)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 索引: 加速分類查詢
CREATE INDEX idx_products_category_id ON products(category_id);

-- 索引: 加速時間排序
CREATE INDEX idx_products_created_at ON products(created_at);

-- 複合索引: 加速前台商品列表查詢 (已上架且未刪除)
CREATE INDEX idx_products_is_published_is_deleted ON products(is_published, is_deleted);

-- 索引: 加速軟刪除篩選
CREATE INDEX idx_products_is_deleted ON products(is_deleted);
