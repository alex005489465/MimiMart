# ====================================================================
# Resend DNS 記錄配置
# ====================================================================
# 用途: 為指定的郵件子域名建立 Resend 郵件服務所需的 DNS 記錄
#
# 包含記錄:
#   - DKIM 驗證記錄
#   - MX 記錄 (用於退信處理)
#   - SPF 記錄
#   - DMARC 記錄 (可選)
#
# 使用說明:
#   1. 在 Resend Dashboard 添加域名並取得 DNS 記錄
#   2. 將記錄值填入 terraform.tfvars
#   3. 執行 terraform apply 自動建立
# ====================================================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }
}

# ====================================================================
# Cloudflare Provider 配置
# ====================================================================

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# ====================================================================
# DKIM 驗證記錄
# ====================================================================

resource "cloudflare_record" "resend_dkim" {
  zone_id = var.cloudflare_zone_id
  name    = var.dkim_record_name
  content = var.dkim_record_value
  type    = "TXT"
  ttl     = var.dns_ttl
  comment = "Resend DKIM 驗證記錄"
}

# ====================================================================
# MX 記錄 (用於退信處理)
# ====================================================================

resource "cloudflare_record" "resend_mx" {
  zone_id  = var.cloudflare_zone_id
  name     = var.mx_record_name
  content  = var.mx_record_value
  type     = "MX"
  priority = var.mx_priority
  ttl      = var.dns_ttl
  comment  = "Resend MX 記錄 (退信處理)"
}

# ====================================================================
# SPF 記錄
# ====================================================================

resource "cloudflare_record" "resend_spf" {
  zone_id = var.cloudflare_zone_id
  name    = var.mail_subdomain
  content = var.spf_record_value
  type    = "TXT"
  ttl     = var.dns_ttl
  comment = "Resend SPF 記錄"
}

# ====================================================================
# DMARC 記錄 (可選,但建議啟用)
# ====================================================================

resource "cloudflare_record" "resend_dmarc" {
  count = var.enable_dmarc ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = var.dmarc_record_name
  content = var.dmarc_policy
  type    = "TXT"
  ttl     = var.dns_ttl
  comment = "Resend DMARC 記錄"
}
