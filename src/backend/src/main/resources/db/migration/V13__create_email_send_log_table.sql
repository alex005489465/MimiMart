-- V13: 建立郵件發送歷史記錄表
-- 用於記錄所有發送的郵件，支援審計和統計分析

CREATE TABLE email_send_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT COMMENT '會員 ID（可為 NULL，如系統通知）',
    recipient_email VARCHAR(255) NOT NULL COMMENT '收件人郵箱',
    email_type VARCHAR(50) NOT NULL COMMENT '郵件類型（WELCOME, VERIFICATION, PASSWORD_RESET）',
    subject VARCHAR(500) NOT NULL COMMENT '郵件主旨',
    sent_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '發送時間',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '發送狀態（SUCCESS, FAILED）',
    error_message TEXT COMMENT '錯誤訊息（如果發送失敗）',

    INDEX idx_member_id (member_id),
    INDEX idx_recipient_email (recipient_email),
    INDEX idx_email_type (email_type),
    INDEX idx_sent_at (sent_at),
    INDEX idx_status (status),

    CONSTRAINT fk_email_send_log_member
        FOREIGN KEY (member_id) REFERENCES members(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='郵件發送歷史記錄表';
