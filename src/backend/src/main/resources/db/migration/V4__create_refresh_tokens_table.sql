-- V4: 建立 refresh_tokens 資料表
-- 用途: JWT Refresh Token 儲存 (用於 Token 撤銷與登出功能,支援前後台雙用戶類型)
-- 技術: MySQL 8.4.6, InnoDB, utf8mb4
-- 設計: 零約束設計 (無 UNIQUE 約束)

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '會員/管理員 ID',
    user_type VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '用戶類型: MEMBER, ADMIN',
    token VARCHAR(512) NOT NULL COMMENT 'JWT Refresh Token',
    expires_at DATETIME(6) NOT NULL COMMENT '過期時間',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
    INDEX idx_member_id (member_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_refresh_tokens_user_type (user_type),
    INDEX idx_refresh_tokens_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token 資料表 (支援前後台雙用戶類型)';
