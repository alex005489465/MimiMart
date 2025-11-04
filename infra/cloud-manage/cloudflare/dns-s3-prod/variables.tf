# ====================================================================
# 變數定義 - 生產環境 S3 DNS
# ====================================================================

# --------------------------------------------------------------------
# Cloudflare 認證資訊
# 注意: 應從 cloudflare/.env 透過環境變數載入
# --------------------------------------------------------------------

variable "cloudflare_api_token" {
  description = "Cloudflare API Token (需要 Zone:DNS:Edit 權限)"
  type        = string
  sensitive   = true
}

variable "cloudflare_zone_id" {
  description = "Cloudflare Zone ID"
  type        = string
}

# --------------------------------------------------------------------
# S3 Public Bucket 子域名
# --------------------------------------------------------------------

variable "public_bucket_subdomain" {
  description = "生產環境 S3 Public Bucket 的子域名 (例如: cdn 或 storage)"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.public_bucket_subdomain)) || var.public_bucket_subdomain == ""
    error_message = "子域名只能包含小寫字母、數字和連字號"
  }
}
