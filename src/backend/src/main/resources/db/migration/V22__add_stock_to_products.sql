-- 在 products 表新增庫存欄位
ALTER TABLE products
ADD COLUMN stock INT DEFAULT 0 COMMENT '商品庫存數量'
AFTER price;
