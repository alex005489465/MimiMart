-- V18: 新增商品啟用/停用欄位
-- 用途: 支援商品啟用/停用功能（與 is_published 分離）

ALTER TABLE products
    ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否啟用 (1=啟用, 0=停用)';

-- 註解:
-- is_active: 商品是否啟用（停用的商品不可上架，後台可見但前台不可見）
-- is_published: 商品是否上架（啟用的商品可選擇上架或下架）
-- 兩者分離可提供更靈活的商品管理
