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
# 郵件子域名配置
# ============================================================================

variable "mail_subdomain" {
  description = "郵件服務子域名（例如: mail ）"
  type        = string
}

# ============================================================================
# DKIM 記錄配置
# ============================================================================

variable "dkim_record_name" {
  description = "DKIM 記錄名稱（例如: resend._domainkey.mail）"
  type        = string
}

variable "dkim_record_value" {
  description = "DKIM 記錄值（從 Resend Dashboard 複製）"
  type        = string
}

# ============================================================================
# MX 記錄配置
# ============================================================================

variable "mx_record_name" {
  description = "MX 記錄名稱（例如: send.mail）"
  type        = string
}

variable "mx_record_value" {
  description = "MX 記錄值（從 Resend Dashboard 複製）"
  type        = string
}

variable "mx_priority" {
  description = "MX 記錄優先級"
  type        = number
  default     = 10
}

# ============================================================================
# SPF 記錄配置
# ============================================================================

variable "spf_record_value" {
  description = "SPF 記錄值（從 Resend Dashboard 複製）"
  type        = string
}

# ============================================================================
# DMARC 配置
# ============================================================================

variable "enable_dmarc" {
  description = "是否啟用 DMARC 記錄"
  type        = bool
  default     = true
}

variable "dmarc_record_name" {
  description = "DMARC 記錄名稱（例如: _dmarc.mail）"
  type        = string
}

variable "dmarc_policy" {
  description = "DMARC 政策內容"
  type        = string
  default     = "v=DMARC1; p=none;"
}

# ============================================================================
# DNS 配置
# ============================================================================

variable "dns_ttl" {
  description = "DNS 記錄 TTL（1 表示自動，Auto）"
  type        = number
  default     = 1
}
