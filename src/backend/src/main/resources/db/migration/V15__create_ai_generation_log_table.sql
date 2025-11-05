-- 建立 AI 生成調用日誌表
CREATE TABLE ai_generation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵',
    admin_id BIGINT NOT NULL COMMENT '調用的管理員 ID',
    api_endpoint VARCHAR(200) NOT NULL COMMENT '調用的後端 API 端點路徑',
    generation_type VARCHAR(20) NOT NULL COMMENT '生成類型: IMAGE, TEXT',
    ai_provider VARCHAR(20) NOT NULL COMMENT 'AI 服務提供商: OPENAI, DEEPSEEK',
    model_name VARCHAR(50) NOT NULL COMMENT '使用的 AI 模型名稱',
    prompt TEXT NOT NULL COMMENT '輸入的 prompt',
    response_content TEXT COMMENT 'AI 回應內容',
    s3_key VARCHAR(500) COMMENT '生成圖片的 S3 路徑 (僅生圖類型)',
    tokens_used INT COMMENT 'Token 用量',
    cost_usd DECIMAL(10, 6) COMMENT '成本 (美元)',
    status VARCHAR(20) NOT NULL COMMENT '狀態: SUCCESS, FAILED',
    error_message TEXT COMMENT '錯誤訊息 (失敗時)',
    created_at DATETIME NOT NULL COMMENT '建立時間',

    INDEX idx_admin_id (admin_id) COMMENT '按管理員查詢',
    INDEX idx_api_endpoint (api_endpoint) COMMENT '按 API 端點查詢',
    INDEX idx_type_provider (generation_type, ai_provider) COMMENT '按類型和提供商查詢',
    INDEX idx_created_at (created_at) COMMENT '按時間查詢'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 生成調用日誌表';
