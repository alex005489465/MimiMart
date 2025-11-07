-- 將訂單編號索引改為唯一索引
-- 確保訂單編號在資料庫層級保證唯一性

-- 刪除舊的普通索引（MySQL 不支援 IF EXISTS，直接執行）
ALTER TABLE orders DROP INDEX idx_order_number;

-- 建立唯一索引
CREATE UNIQUE INDEX idx_order_number ON orders(order_number);
