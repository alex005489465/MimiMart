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
# DNS 記錄管理
# ============================================================================
# 管理 Cloudflare DNS 記錄，支援多種記錄類型（A、AAAA、CNAME、MX、TXT 等）
# 用於配置與 Cloudflare Pages 無關的獨立 DNS 記錄

resource "cloudflare_record" "records" {
  for_each = { for record in var.dns_records : record.name => record }

  zone_id = var.cloudflare_zone_id
  name    = each.value.name
  content = each.value.content
  type    = each.value.type
  proxied = lookup(each.value, "proxied", false)
  ttl     = lookup(each.value, "ttl", 1)
  priority = lookup(each.value, "priority", null)
  comment = lookup(each.value, "comment", null)
}
