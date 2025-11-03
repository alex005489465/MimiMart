# ============================================================================
# Pages 專案輸出
# ============================================================================

output "pages_project_id" {
  description = "Pages 專案 ID"
  value       = cloudflare_pages_project.admin_frontend.id
}

output "pages_project_name" {
  description = "Pages 專案名稱"
  value       = cloudflare_pages_project.admin_frontend.name
}

output "pages_subdomain" {
  description = "Pages 自動生成的子域名（*.pages.dev）"
  value       = cloudflare_pages_project.admin_frontend.subdomain
}

output "pages_url" {
  description = "Pages 專案的完整 URL（自動域名）"
  value       = "https://${cloudflare_pages_project.admin_frontend.subdomain}"
}

output "custom_domain" {
  description = "自訂域名"
  value       = cloudflare_pages_domain.admin_frontend_custom_domain.domain
}

output "custom_domain_url" {
  description = "自訂域名的完整 URL"
  value       = "https://${cloudflare_pages_domain.admin_frontend_custom_domain.domain}"
}

output "deployment_info" {
  description = "部署資訊摘要"
  value = {
    project_name      = cloudflare_pages_project.admin_frontend.name
    pages_dev_url     = "https://${cloudflare_pages_project.admin_frontend.subdomain}"
    custom_url        = "https://${cloudflare_pages_domain.admin_frontend_custom_domain.domain}"
    production_branch = cloudflare_pages_project.admin_frontend.production_branch
  }
}

# ============================================================================
# 用於 DNS 模組引用
# ============================================================================

output "pages_subdomain_for_dns" {
  description = "Pages 自動域名（供 DNS 模組 CNAME 記錄使用）"
  value       = cloudflare_pages_project.admin_frontend.subdomain
}
