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
# Admin Frontend Cloudflare Pages 專案
# ============================================================================
# 管理 Admin Frontend Pages 專案配置
# 用於部署管理後台前端單頁應用程式（SPA）

resource "cloudflare_pages_project" "admin_frontend" {
  account_id        = var.cloudflare_account_id
  name              = var.pages_project_name
  production_branch = var.production_branch
}

resource "cloudflare_pages_domain" "admin_frontend_custom_domain" {
  account_id   = var.cloudflare_account_id
  project_name = cloudflare_pages_project.admin_frontend.name
  domain       = var.custom_domain
}

# ============================================================================
# Shop Frontend Cloudflare Pages 專案
# ============================================================================
# 管理 Shop Frontend Pages 專案配置
# 用於部署商城前端單頁應用程式（SPA）

resource "cloudflare_pages_project" "shop_frontend" {
  account_id        = var.cloudflare_account_id
  name              = var.shop_pages_project_name
  production_branch = var.shop_production_branch
}

resource "cloudflare_pages_domain" "shop_frontend_custom_domain" {
  account_id   = var.cloudflare_account_id
  project_name = cloudflare_pages_project.shop_frontend.name
  domain       = var.shop_custom_domain
}
