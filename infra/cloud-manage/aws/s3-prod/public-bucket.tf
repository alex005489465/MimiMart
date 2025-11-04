# ====================================================================
# Public Bucket
# ====================================================================
# 用途: 存放公開可存取的混合資源
# 存取方式: 通過 Cloudflare CDN 公開讀取，後端可 CRUD
# 安全性: 僅允許 Cloudflare IP + 開發者 IP 存取
# ====================================================================

# --------------------------------------------------------------------
# 引用 IP 範圍模組
# --------------------------------------------------------------------

module "ip_ranges" {
  source = "../../modules/ip-ranges"
}

# --------------------------------------------------------------------
# S3 Bucket
# --------------------------------------------------------------------

resource "aws_s3_bucket" "public" {
  bucket = var.public_bucket_name

  tags = merge(
    local.common_tags,
    {
      Name           = var.public_bucket_name
      Description    = "Public bucket for mixed resources"
      Public         = "true"
      AccessType     = "IPRestricted"
    }
  )
}

# --------------------------------------------------------------------
# 版本控制
# --------------------------------------------------------------------

resource "aws_s3_bucket_versioning" "public" {
  bucket = aws_s3_bucket.public.id

  versioning_configuration {
    status = var.public_enable_versioning ? "Enabled" : "Disabled"
  }
}

# --------------------------------------------------------------------
# 伺服器端加密 (SSE-S3)
# --------------------------------------------------------------------

resource "aws_s3_bucket_server_side_encryption_configuration" "public" {
  bucket = aws_s3_bucket.public.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# --------------------------------------------------------------------
# 靜態網站託管配置
#
# 啟用靜態網站託管以獲得 website endpoint
# 用於 Cloudflare CNAME 記錄
# --------------------------------------------------------------------

resource "aws_s3_bucket_website_configuration" "public" {
  bucket = aws_s3_bucket.public.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "error.html"
  }
}

# --------------------------------------------------------------------
# 公開存取區塊設定
#
# 允許 bucket policy，但不允許 ACL
# 透過 bucket policy 控制存取（限制特定 IP）
# --------------------------------------------------------------------

resource "aws_s3_bucket_public_access_block" "public" {
  bucket = aws_s3_bucket.public.id

  block_public_acls       = true
  block_public_policy     = false  # 允許 public bucket policy
  ignore_public_acls      = true
  restrict_public_buckets = false  # 允許公開讀取（但僅限特定 IP）
}

# --------------------------------------------------------------------
# Bucket Policy - 限制 IP 存取 (生產環境)
#
# 允許公開讀取，但僅限 Cloudflare IP
# 這確保了公開資源只能透過 CDN 存取，提供最高安全性
#
# IP 來源:
#   - Cloudflare IPv4 範圍 (15 個 CIDR)
#   - Cloudflare IPv6 範圍 (7 個 CIDR)
# --------------------------------------------------------------------

resource "aws_s3_bucket_policy" "public_cloudflare_only" {
  bucket = aws_s3_bucket.public.id

  # 確保公開存取設定先完成
  depends_on = [
    aws_s3_bucket_public_access_block.public
  ]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowCloudflareIPsOnly"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.public.arn}/*"
        Condition = {
          IpAddress = {
            # 僅 Cloudflare IP (IPv4 + IPv6)
            "aws:SourceIp" = module.ip_ranges.cloudflare_all
          }
        }
      }
    ]
  })
}

# --------------------------------------------------------------------
# CORS 配置
#
# 允許前端（包含本地開發）跨域存取公開資源
# 開發環境配置: localhost + Cloudflare CDN
# --------------------------------------------------------------------

resource "aws_s3_bucket_cors_configuration" "public" {
  bucket = aws_s3_bucket.public.id

  cors_rule {
    allowed_headers = var.public_cors_allowed_headers
    allowed_methods = var.public_cors_allowed_methods
    allowed_origins = var.public_cors_allowed_origins
    max_age_seconds = var.public_cors_max_age_seconds
    expose_headers  = ["ETag"]
  }
}
