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
# Admin Frontend Pages 專案配置
# ============================================================================

variable "pages_project_name" {
  description = "Admin Pages 專案名稱（只能包含小寫字母、數字和連字符）"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.pages_project_name))
    error_message = "專案名稱只能包含小寫字母、數字和連字符"
  }
}

variable "production_branch" {
  description = "Admin 生產分支名稱"
  type        = string
}

variable "custom_domain" {
  description = "Admin Pages 專案的完整自訂域名（例如: admin.example.com）"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9.-]+\\.[a-z]{2,}$", var.custom_domain))
    error_message = "域名格式不正確"
  }
}

# ============================================================================
# Shop Frontend Pages 專案配置
# ============================================================================

variable "shop_pages_project_name" {
  description = "Shop Pages 專案名稱（只能包含小寫字母、數字和連字符）"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.shop_pages_project_name))
    error_message = "專案名稱只能包含小寫字母、數字和連字符"
  }
}

variable "shop_production_branch" {
  description = "Shop 生產分支名稱"
  type        = string
}

variable "shop_custom_domain" {
  description = "Shop Pages 專案的完整自訂域名（例如: shop.example.com）"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9.-]+\\.[a-z]{2,}$", var.shop_custom_domain))
    error_message = "域名格式不正確"
  }
}
