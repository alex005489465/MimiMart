terraform {
  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }
  required_version = ">= 1.0"
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# ============================================================================
# Cloudflare Pages 專案
# ============================================================================
# 管理 Cloudflare Pages 專案配置
# 用於部署前端單頁應用程式（SPA）

resource "cloudflare_pages_project" "admin_frontend" {
  account_id        = var.cloudflare_account_id
  name              = var.pages_project_name
  production_branch = var.production_branch
}

# ============================================================================
# 自訂域名配置
# ============================================================================
# 為 Pages 專案配置自訂域名
# DNS 記錄統一在 dns 模組管理

resource "cloudflare_pages_domain" "admin_frontend_custom_domain" {
  account_id   = var.cloudflare_account_id
  project_name = cloudflare_pages_project.admin_frontend.name
  domain       = var.custom_domain
}
