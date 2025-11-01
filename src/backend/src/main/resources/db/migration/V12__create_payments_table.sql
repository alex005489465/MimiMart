-- V12: 建立付款相關資料表
-- 符合零約束設計原則: 無外鍵、無唯一索引、無 CHECK 約束

-- 付款記錄表
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '訂單ID (無外鍵約束)',
    payment_number VARCHAR(50) NOT NULL COMMENT '付款編號 (PAY + 時間戳 + 隨機碼)',
    payment_method VARCHAR(30) NOT NULL COMMENT '付款方式: ECPAY_Credit, ECPAY_ATM, etc.',
    amount DECIMAL(10, 2) NOT NULL COMMENT '付款金額',
    status VARCHAR(20) NOT NULL COMMENT '付款狀態: PENDING_PAYMENT, PAID, EXPIRED, CANCELLED',
    external_transaction_id VARCHAR(100) COMMENT '第三方交易流水號 (綠界 TradeNo)',
    paid_at TIMESTAMP NULL COMMENT '實際付款完成時間',
    expired_at TIMESTAMP NOT NULL COMMENT '付款截止時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP NULL COMMENT '更新時間',
    INDEX idx_order_id (order_id),
    INDEX idx_payment_number (payment_number),
    INDEX idx_status (status),
    INDEX idx_expired_at (expired_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款記錄表';
