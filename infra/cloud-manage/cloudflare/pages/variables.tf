# ============================================================================
# Cloudflare 認證資訊
# ============================================================================

variable "cloudflare_api_token" {
  description = "Cloudflare API Token（需要 Account:Cloudflare Pages:Edit 權限）"
  type        = string
  sensitive   = true
}

variable "cloudflare_account_id" {
  description = "Cloudflare Account ID"
  type        = string
}

# ============================================================================
# Pages 專案配置
# ============================================================================

variable "pages_project_name" {
  description = "Pages 專案名稱（只能包含小寫字母、數字和連字符）"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.pages_project_name))
    error_message = "專案名稱只能包含小寫字母、數字和連字符"
  }
}

variable "production_branch" {
  description = "生產分支名稱"
  type        = string
}

# ============================================================================
# 域名配置
# ============================================================================

variable "custom_domain" {
  description = "Pages 專案的完整自訂域名（例如: admin.example.com）"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9.-]+\\.[a-z]{2,}$", var.custom_domain))
    error_message = "域名格式不正確"
  }
}
