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
# DNS 記錄配置
# ============================================================================

variable "dns_records" {
  description = "DNS 記錄列表"
  type = list(object({
    name     = string           # DNS 記錄名稱，基礎域名之前的部分（例如：git-ui.mimimart、api、@）
    type     = string           # 記錄類型（A、AAAA、CNAME、MX、TXT 等）
    content  = string           # 記錄內容（IP 地址、域名或文字）
    proxied  = optional(bool)   # 是否啟用 Cloudflare 代理（預設：false）
    ttl      = optional(number) # TTL 值（秒），1 表示自動（預設：1）
    priority = optional(number) # 優先級（僅 MX 和 SRV 記錄需要）
    comment  = optional(string) # 記錄備註說明
  }))

  validation {
    condition = alltrue([
      for record in var.dns_records :
      contains(["A", "AAAA", "CNAME", "MX", "TXT", "SRV", "CAA", "NS", "PTR"], record.type)
    ])
    error_message = "記錄類型必須是 A、AAAA、CNAME、MX、TXT、SRV、CAA、NS 或 PTR"
  }

  validation {
    condition = alltrue([
      for record in var.dns_records :
      record.name != "" && record.content != ""
    ])
    error_message = "DNS 記錄的 name 和 content 不能為空"
  }
}
