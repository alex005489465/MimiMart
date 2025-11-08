-- V23: 建立物流資料表
-- 符合零約束設計原則: 無外鍵、無唯一索引、無 CHECK 約束

-- 物流資料表
CREATE TABLE shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '訂單ID (無外鍵約束)',

    -- 前台會員建立訂單時填寫（來自 DeliveryInfo）
    receiver_name VARCHAR(50) NOT NULL COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    shipping_address VARCHAR(200) NOT NULL COMMENT '配送地址',
    delivery_method VARCHAR(30) NOT NULL COMMENT '配送方式: HOME_DELIVERY/STORE_PICKUP',
    delivery_note TEXT NULL COMMENT '配送備註',
    shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '運費',

    -- 後台管理員出貨時填寫
    carrier VARCHAR(50) NULL COMMENT '物流商名稱 (黑貓/新竹/順豐等)',
    tracking_number VARCHAR(100) NULL COMMENT '物流追蹤號碼',
    shipped_at TIMESTAMP NULL COMMENT '實際出貨時間',
    estimated_delivery_date DATE NULL COMMENT '預計送達日期',

    -- 配送狀態管理
    delivery_status VARCHAR(30) NOT NULL COMMENT '配送狀態: PREPARING/SHIPPED/IN_TRANSIT/OUT_FOR_DELIVERY/DELIVERED/FAILED',
    actual_delivery_date TIMESTAMP NULL COMMENT '實際送達時間',
    notes TEXT NULL COMMENT '物流處理備註',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NULL COMMENT '更新時間',

    INDEX idx_order_id (order_id),
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_delivery_status (delivery_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流資料表';
