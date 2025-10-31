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
