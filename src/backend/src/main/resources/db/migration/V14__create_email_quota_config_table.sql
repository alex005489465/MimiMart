-- V14: 建立郵件配額配置表
-- 用於管理郵件發送配額和統計資訊

CREATE TABLE email_quota_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置鍵（如：monthly_limit, current_month）',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置說明',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新時間',

    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='郵件配額配置表';

-- 插入初始配置
INSERT INTO email_quota_config (config_key, config_value, description) VALUES
('monthly_limit', '1000', '每月發信上限'),
('alert_threshold_80', 'false', '80% 告警已發送標記'),
('alert_threshold_90', 'false', '90% 告警已發送標記'),
('alert_threshold_100', 'false', '100% 告警已發送標記');
