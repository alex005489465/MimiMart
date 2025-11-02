# ============================================================================
# Cloudflare 認證變數
# ============================================================================
variable "cloudflare_api_token" {
  description = "Cloudflare API Token"
  type        = string
  sensitive   = true
}

variable "cloudflare_zone_id" {
  description = "Cloudflare Zone ID"
  type        = string
}

variable "domain_name" {
  description = "基礎域名（從 .env 的 TF_VAR_domain_name 取得）"
  type        = string
}

# ============================================================================
# WAF 規則配置
# ============================================================================
variable "waf_rules" {
  description = "WAF 自訂規則列表"
  type = list(object({
    action      = string
    expression  = string
    description = string
    enabled     = optional(bool, true)
  }))
  default = []
}

# ============================================================================
# Storage Protection 配置
# ============================================================================
variable "enable_storage_protection" {
  description = "是否啟用 Storage 子域名的 IP 限制保護"
  type        = bool
  default     = false
}

variable "storage_subdomain" {
  description = "要保護的 Storage 子域名（不含域名後綴）"
  type        = string
  default     = ""
}
