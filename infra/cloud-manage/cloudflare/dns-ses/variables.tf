# ============================================================================
# Cloudflare 認證資訊
# ============================================================================

variable "cloudflare_api_token" {
  description = "Cloudflare API Token（需要 Zone:DNS:Edit 權限）"
  type        = string
  sensitive   = true
}

variable "cloudflare_zone_id" {
  description = "Cloudflare Zone ID"
  type        = string
}

# ============================================================================
# 域名配置
# ============================================================================

variable "domain_name" {
  description = "基礎域名（例如: example.com）"
  type        = string
}

# ============================================================================
# DMARC 配置
# ============================================================================

variable "dmarc_policy" {
  description = "DMARC 政策 (none/quarantine/reject)"
  type        = string
  default     = "quarantine"
  validation {
    condition     = contains(["none", "quarantine", "reject"], var.dmarc_policy)
    error_message = "DMARC 政策必須是 none、quarantine 或 reject"
  }
}

variable "dmarc_rua_email" {
  description = "接收 DMARC 彙整報告的郵箱（可選）"
  type        = string
  default     = ""
}

variable "dmarc_ruf_email" {
  description = "接收 DMARC 失敗報告的郵箱（可選）"
  type        = string
  default     = ""
}
