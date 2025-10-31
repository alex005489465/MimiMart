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
# WAF Custom Rules - IP 白名單規則
# ============================================================================
resource "cloudflare_ruleset" "waf_custom_rules" {
  zone_id     = var.cloudflare_zone_id
  name        = "WAF Custom Rules - IP Whitelist"
  description = "限制特定域名只能從允許的 IP 訪問"
  kind        = "zone"
  phase       = "http_request_firewall_custom"

  dynamic "rules" {
    for_each = var.waf_rules
    content {
      action      = rules.value.action
      expression  = rules.value.expression
      description = rules.value.description
      enabled     = lookup(rules.value, "enabled", true)
    }
  }
}
