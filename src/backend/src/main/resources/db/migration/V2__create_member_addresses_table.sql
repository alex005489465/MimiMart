-- V2: 建立會員收貨地址資料表
-- 用途: 儲存會員的多組收貨地址資訊
-- 技術: MySQL 8.4.6, InnoDB, utf8mb4
-- 設計: 零約束設計 (無外鍵約束,應用程式層負責)

CREATE TABLE member_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '會員 ID',
    recipient_name VARCHAR(100) NOT NULL COMMENT '收件人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    address TEXT NOT NULL COMMENT '收貨地址',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為預設地址',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 索引: 加速會員地址查詢
CREATE INDEX idx_member_addresses_member_id ON member_addresses(member_id);

-- 索引: 加速預設地址查詢
CREATE INDEX idx_member_addresses_is_default ON member_addresses(member_id, is_default);

-- 表註解
ALTER TABLE member_addresses COMMENT = '會員收貨地址表';
