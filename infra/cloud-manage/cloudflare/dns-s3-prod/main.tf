# ====================================================================
# Cloudflare DNS 模組 - 生產環境 S3 Bucket
# ====================================================================
# 用途: 自動建立生產環境 S3 Public Bucket 的 DNS 記錄
#
# 工作原理:
#   1. 讀取 aws/s3-prod 模組的 terraform.tfstate
#   2. 取得 public_bucket_website_endpoint
#   3. 建立 CNAME 記錄指向 S3 Website Endpoint
#   4. 啟用 Cloudflare Proxy (CDN + SSL)
#
# 依賴順序:
#   aws/s3-prod → cloudflare/dns-s3-prod
# ====================================================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }
}

# ====================================================================
# Cloudflare Provider 配置
# ====================================================================

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}
