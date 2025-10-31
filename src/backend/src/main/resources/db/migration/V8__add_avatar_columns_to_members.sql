-- 新增會員頭像相關欄位
ALTER TABLE members
ADD COLUMN avatar_url VARCHAR(500) COMMENT '頭像 CloudFront URL',
ADD COLUMN avatar_s3_key VARCHAR(500) COMMENT '頭像 S3 Key',
ADD COLUMN avatar_updated_at DATETIME(6) COMMENT '頭像更新時間';

-- 新增索引以提升查詢效能
CREATE INDEX idx_members_avatar_updated_at ON members(avatar_updated_at);
