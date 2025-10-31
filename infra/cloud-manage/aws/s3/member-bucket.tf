# ====================================================================
# Member Avatar Bucket
# ====================================================================
# 用途: 存放會員大頭照
# 存取方式: 通過 Cloudflare CDN 公開讀取,後端可 CRUD
# ====================================================================

# --------------------------------------------------------------------
# S3 Bucket
# --------------------------------------------------------------------

resource "aws_s3_bucket" "member" {
  bucket = var.member_bucket_name

  tags = merge(
    local.common_tags,
    {
      Name        = var.member_bucket_name
      Description = "Member avatar bucket for user profile photos"
      Public      = "true"
    }
  )
}

# --------------------------------------------------------------------
# 版本控制
# --------------------------------------------------------------------

resource "aws_s3_bucket_versioning" "member" {
  bucket = aws_s3_bucket.member.id

  versioning_configuration {
    status = var.member_enable_versioning ? "Enabled" : "Disabled"
  }
}

# --------------------------------------------------------------------
# 伺服器端加密 (SSE-S3)
# --------------------------------------------------------------------

resource "aws_s3_bucket_server_side_encryption_configuration" "member" {
  bucket = aws_s3_bucket.member.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# --------------------------------------------------------------------
# 公開存取區塊設定
#
# 完全阻止公開訪問,所有存取必須通過後端 API
# --------------------------------------------------------------------

resource "aws_s3_bucket_public_access_block" "member" {
  bucket = aws_s3_bucket.member.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# --------------------------------------------------------------------
# CORS 配置
#
# 允許前端(包含本地開發)跨域存取會員頭像
# 開發環境配置: localhost + Cloudflare CDN
# --------------------------------------------------------------------

resource "aws_s3_bucket_cors_configuration" "member" {
  bucket = aws_s3_bucket.member.id

  cors_rule {
    allowed_headers = var.member_cors_allowed_headers
    allowed_methods = var.member_cors_allowed_methods
    allowed_origins = var.member_cors_allowed_origins
    max_age_seconds = var.member_cors_max_age_seconds
    expose_headers  = ["ETag"]
  }
}
