-- 建立訂單主表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '會員 ID',
    order_number VARCHAR(50) NOT NULL COMMENT '訂單編號(格式:ORD+時間戳+隨機碼)',
    status VARCHAR(20) NOT NULL COMMENT '訂單狀態:PAYMENT_PENDING,PAID,SHIPPED,COMPLETED,CANCELLED',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '訂單總金額',

    -- 送貨資訊(JSON 格式儲存)
    delivery_info TEXT NOT NULL COMMENT '送貨資訊 JSON:{receiverName,receiverPhone,shippingAddress,deliveryMethod,deliveryNote}',

    -- 取消原因
    cancellation_reason VARCHAR(500) NULL COMMENT '訂單取消原因(僅 CANCELLED 狀態時有值)',

    -- 時間戳記
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單主表';

-- 訂單編號索引(唯一性由應用層保證)
CREATE INDEX idx_order_number ON orders(order_number);

-- 會員訂單列表查詢索引(會員 ID + 建立時間降序)
CREATE INDEX idx_member_created ON orders(member_id, created_at DESC);

-- 訂單狀態索引(後台篩選用)
CREATE INDEX idx_status ON orders(status);
