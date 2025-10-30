-- 可重複執行的測試資料腳本
-- 用途: 開發環境測試資料 (會員、管理員)
-- 技術: MySQL 8.4.6
-- 注意: R__ 開頭的腳本會在每次 Flyway 執行時重新執行

-- 清空現有測試資料 (保持資料庫乾淨)
DELETE FROM refresh_tokens WHERE member_id IN (SELECT id FROM members WHERE email LIKE '%@test.com');
DELETE FROM refresh_tokens WHERE member_id IN (SELECT id FROM admins WHERE username LIKE 'test%');
DELETE FROM member_addresses WHERE member_id IN (SELECT id FROM members WHERE email LIKE '%@test.com');
DELETE FROM members WHERE email LIKE '%@test.com';
DELETE FROM admins WHERE username LIKE 'test%';

-- 插入測試會員
-- 密碼: password123 (BCrypt 加密, strength 10)
INSERT INTO members (email, password_hash, name, phone, home_address, status, email_verified, created_at) VALUES
('member1@test.com', '$2a$10$rN8qGKT3Ux3XaL5j5YYbDOZxGxWQXF0Vxq0.0PH7Q7K7ZXxYWzXxC', '測試會員一', '0912345678', '台北市信義區測試路 1 號', 'ACTIVE', TRUE, NOW()),
('member2@test.com', '$2a$10$rN8qGKT3Ux3XaL5j5YYbDOZxGxWQXF0Vxq0.0PH7Q7K7ZXxYWzXxC', '測試會員二', '0923456789', '新北市板橋區測試路 2 號', 'ACTIVE', TRUE, NOW()),
('member3@test.com', '$2a$10$rN8qGKT3Ux3XaL5j5YYbDOZxGxWQXF0Vxq0.0PH7Q7K7ZXxYWzXxC', '測試會員三', '0934567890', '台中市西屯區測試路 3 號', 'ACTIVE', FALSE, NOW());

-- 插入測試管理員
-- 密碼: admin123 (BCrypt 加密, strength 10)
INSERT INTO admins (username, email, password_hash, name, status, created_at) VALUES
('testadmin', 'admin@test.com', '$2a$10$rN8qGKT3Ux3XaL5j5YYbDOZxGxWQXF0Vxq0.0PH7Q7K7ZXxYWzXxC', '測試管理員', 'ACTIVE', NOW()),
('testadmin2', 'admin2@test.com', '$2a$10$rN8qGKT3Ux3XaL5j5YYbDOZxGxWQXF0Vxq0.0PH7Q7K7ZXxYWzXxC', '測試管理員二', 'ACTIVE', NOW());

-- 插入測試地址
INSERT INTO member_addresses (member_id, recipient_name, phone, address, is_default, created_at, updated_at)
SELECT id, '測試收件人一', '0912345678', '台北市信義區測試路 1 號 1 樓', TRUE, NOW(), NOW()
FROM members WHERE email = 'member1@test.com'
UNION ALL
SELECT id, '測試收件人二', '0912345678', '台北市信義區測試路 1 號 2 樓', FALSE, NOW(), NOW()
FROM members WHERE email = 'member1@test.com'
UNION ALL
SELECT id, '測試收件人三', '0923456789', '新北市板橋區測試路 2 號', TRUE, NOW(), NOW()
FROM members WHERE email = 'member2@test.com';

-- 測試帳號說明
-- 會員帳號:
--   Email: member1@test.com, member2@test.com, member3@test.com
--   密碼: password123
-- 管理員帳號:
--   Username: testadmin, testadmin2
--   密碼: admin123
