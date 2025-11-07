-- V17: 新增商品上下架時間排程欄位
-- 用途: 支援商品定時上下架功能

ALTER TABLE products
    ADD COLUMN published_at DATETIME DEFAULT NULL COMMENT '上架時間 (NULL 表示不限制)',
    ADD COLUMN unpublished_at DATETIME DEFAULT NULL COMMENT '下架時間 (NULL 表示不限制)';

-- 註解:
-- published_at: 商品開始顯示於前台的時間
-- unpublished_at: 商品停止顯示於前台的時間
-- 兩者皆為 NULL 表示永久上架（只受 is_published 控制）
