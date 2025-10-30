-- V1: 建立 members 資料表
-- 用途: 儲存會員的基本資訊、憑證和狀態
-- 技術: MySQL 8.4.6, InnoDB, utf8mb4
-- 設計: 零約束設計 (無 UNIQUE、CHECK 約束,應用程式層負責)

CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    phone VARCHAR(20),
    home_address TEXT COMMENT '住家地址',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '帳號狀態: ACTIVE, DISABLED, BANNED',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '電子郵件是否已驗證',
    verification_token VARCHAR(100) COMMENT '驗證 Token',
    verification_token_expires_at DATETIME(6) COMMENT '驗證 Token 過期時間',
    password_reset_token VARCHAR(100) COMMENT '密碼重設 Token',
    password_reset_token_expires_at DATETIME(6) COMMENT '密碼重設 Token 過期時間',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_login_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 索引: 加速登入查詢
CREATE INDEX idx_members_email ON members(email);

-- 索引: 支援狀態篩選
CREATE INDEX idx_members_status ON members(status);

-- 索引: 加速 Email 驗證 Token 查詢
CREATE INDEX idx_members_verification_token ON members(verification_token);

-- 索引: 加速密碼重設 Token 查詢
CREATE INDEX idx_members_password_reset_token ON members(password_reset_token);

-- 索引: 支援後台統計報表 (按登入時間排序)
CREATE INDEX idx_members_last_login_at ON members(last_login_at);

-- 表註解
ALTER TABLE members COMMENT = '會員資料表';
