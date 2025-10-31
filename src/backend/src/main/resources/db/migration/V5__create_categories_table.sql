-- V5: 建立商品分類資料表
-- 設計: 零約束設計 (無 UNIQUE、CHECK 約束,應用程式層負責)

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '分類名稱',
    description VARCHAR(500) COMMENT '分類描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序權重 (數字越小越前面)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '軟刪除時間戳 (NULL=未刪除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 索引: 加速排序查詢
CREATE INDEX idx_categories_sort_order ON categories(sort_order);

-- 索引: 加速軟刪除篩選
CREATE INDEX idx_categories_deleted_at ON categories(deleted_at);
