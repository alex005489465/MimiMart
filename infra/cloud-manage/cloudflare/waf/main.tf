terraform {
  required_version = ">= 1.0"

  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }
}

# ============================================================================
# Provider 配置
# ============================================================================
provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# ============================================================================
# 引用 IP Ranges 模組
# ============================================================================
module "ip_ranges" {
  source = "../../modules/ip-ranges"
}

# ============================================================================
# 本地變數 - 動態生成 WAF 規則
# ============================================================================
locals {
  # 將 admin_only IP 列表格式化為 Cloudflare 表達式格式
  admin_ips_formatted = join(" ", module.ip_ranges.admin_only)

  # 組合自訂規則和動態生成的規則
  all_waf_rules = concat(
    var.waf_rules,
    var.enable_storage_protection ? [
      {
        action      = "block"
        expression  = "(http.host eq \"${var.storage_subdomain}.${var.domain_name}\" and not ip.src in {${local.admin_ips_formatted}})"
        description = "限制 ${var.storage_subdomain} 只能從管理員 IP 訪問"
        enabled     = true
      }
    ] : [],
    var.enable_phpmyadmin_protection ? [
      {
        action      = "block"
        expression  = "(http.host eq \"${var.phpmyadmin_subdomain}.${var.domain_name}\" and not ip.src in {${local.admin_ips_formatted}})"
        description = "限制 ${var.phpmyadmin_subdomain} 只能從管理員 IP 訪問"
        enabled     = true
      }
    ] : []
  )
}

# ============================================================================
# WAF Custom Rules - IP 白名單規則
# ============================================================================
resource "cloudflare_ruleset" "waf_custom_rules" {
  zone_id     = var.cloudflare_zone_id
  name        = "WAF Custom Rules - IP Whitelist"
  description = "限制特定域名只能從允許的 IP 訪問"
  kind        = "zone"
  phase       = "http_request_firewall_custom"

  dynamic "rules" {
    for_each = local.all_waf_rules
    content {
      action      = rules.value.action
      expression  = rules.value.expression
      description = rules.value.description
      enabled     = lookup(rules.value, "enabled", true)
    }
  }
}
