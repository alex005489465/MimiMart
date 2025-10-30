-- V3: 建立 admins 資料表
-- 用途: 儲存後台管理員的基本資訊與憑證
-- 技術: MySQL 8.4.6, InnoDB, utf8mb4
-- 設計: 零約束設計 + 簡化版 (無複雜權限系統)

CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '管理員帳號',
    email VARCHAR(255) NOT NULL COMMENT '管理員 Email',
    password_hash VARCHAR(255) NOT NULL COMMENT '密碼雜湊值',
    name VARCHAR(100) COMMENT '管理員姓名',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '帳號狀態: ACTIVE, DISABLED, BANNED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
    last_login_at DATETIME(6) COMMENT '最後登入時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='後台管理員資料表';

-- 索引: 加速登入查詢 (username)
CREATE INDEX idx_admins_username ON admins(username);

-- 索引: 加速登入查詢 (email)
CREATE INDEX idx_admins_email ON admins(email);

-- 索引: 支援狀態篩選
CREATE INDEX idx_admins_status ON admins(status);
