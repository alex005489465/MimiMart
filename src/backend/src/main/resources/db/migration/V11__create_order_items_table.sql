-- 建立訂單項目表
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '訂單 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',

    -- 商品快照資料(JSON 格式儲存)
    snapshot_data TEXT NOT NULL COMMENT '商品快照 JSON:{productName,productPrice,productOriginalPrice,productImage}',

    -- 訂單項目資訊
    quantity INT NOT NULL COMMENT '購買數量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小計金額(price * quantity)',

    -- 時間戳記
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單項目表';

-- 訂單項目查詢索引(根據訂單 ID 查詢所有項目)
CREATE INDEX idx_order_id ON order_items(order_id);

-- 商品索引(查詢某商品的銷售記錄)
CREATE INDEX idx_product_id ON order_items(product_id);
