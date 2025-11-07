-- 移除商品原價欄位，簡化價格結構
-- 只保留 price 欄位作為商品售價

ALTER TABLE products
    DROP COLUMN original_price;

-- 更新欄位註解
ALTER TABLE products
    MODIFY COLUMN price DECIMAL(10, 2) NOT NULL COMMENT '商品售價';
