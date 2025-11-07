-- =====================================================
-- 輪播圖上架/下架時間功能
-- =====================================================
-- 用途: 新增輪播圖的上架和下架時間欄位，支援自動化排程管理
-- 功能:
--   1. published_at: 上架時間（NULL 表示立即上架）
--   2. unpublished_at: 下架時間（NULL 表示永不下架）
--   3. 前台僅顯示已上架且未下架的輪播圖

ALTER TABLE banners
    ADD COLUMN published_at DATETIME(6) NULL COMMENT '上架時間 (NULL 表示立即上架)',
    ADD COLUMN unpublished_at DATETIME(6) NULL COMMENT '下架時間 (NULL 表示永不下架)';

-- 新增複合索引，優化前台查詢已上架且未下架的輪播圖
CREATE INDEX idx_publish_schedule ON banners (status, published_at, unpublished_at);
