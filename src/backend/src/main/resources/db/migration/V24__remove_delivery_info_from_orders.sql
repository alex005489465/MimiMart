-- V24: 從 orders 表移除 delivery_info 欄位
-- 配送資訊現在統一由 shipments 表管理

ALTER TABLE orders DROP COLUMN delivery_info;
