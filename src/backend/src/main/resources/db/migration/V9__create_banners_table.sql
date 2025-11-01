-- =====================================================
-- 輪播圖資料表
-- =====================================================
-- 用途: 儲存網站首頁輪播圖資訊
-- 設計原則: 零約束設計 (無外鍵、無 UNIQUE 索引除主鍵外)

CREATE TABLE banners
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '輪播圖 ID',
    title         VARCHAR(100)  NOT NULL COMMENT '輪播圖標題',
    image_url     VARCHAR(500)  NOT NULL COMMENT 'S3 圖片 URL',
    link_url      VARCHAR(500) COMMENT '點擊跳轉連結 (可為 null)',
    display_order INT           NOT NULL DEFAULT 0 COMMENT '顯示順序 (數字越小越優先顯示)',
    status        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態: ACTIVE(啟用) / INACTIVE(停用)',
    created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
    updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新時間',

    INDEX idx_status_order (status, display_order) COMMENT '優化前台查詢啟用輪播圖並按順序排序'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '輪播圖資料表';
