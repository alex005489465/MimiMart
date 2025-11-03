# ====================================================================
# AWS SES DNS 記錄自動化
# ====================================================================
# 用途: 自動建立 AWS SES 所需的 Cloudflare DNS 記錄
# 方式: 透過 terraform_remote_state 讀取 SES 模組的輸出
#
# 包含記錄:
#   - 域名驗證 TXT 記錄
#   - DKIM CNAME 記錄 (3 組)
#   - MAIL FROM MX 記錄
#   - MAIL FROM SPF TXT 記錄
#   - DMARC TXT 記錄
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
# 讀取 SES 模組的 State
# ====================================================================

data "terraform_remote_state" "ses" {
  backend = "local"

  config = {
    path = "../../aws/ses/terraform.tfstate"
  }
}

# ====================================================================
# 本地變數
# ====================================================================

locals {
  # 從 SES state 讀取 DNS 記錄需求
  ses_dns_records = try(data.terraform_remote_state.ses.outputs.dns_records, null)

  # 檢查 SES 是否已部署
  ses_deployed = local.ses_dns_records != null

  # 域名驗證記錄
  domain_verification = try(local.ses_dns_records.domain_verification, null)

  # DKIM 記錄
  dkim_records = try(local.ses_dns_records.dkim_records, [])

  # MAIL FROM 記錄
  mail_from_mx  = try(local.ses_dns_records.mail_from_mx, null)
  mail_from_spf = try(local.ses_dns_records.mail_from_spf, null)

  # DMARC 記錄內容
  dmarc_record = join("; ", concat(
    ["v=DMARC1", "p=${var.dmarc_policy}"],
    var.dmarc_rua_email != "" ? ["rua=mailto:${var.dmarc_rua_email}"] : [],
    var.dmarc_ruf_email != "" ? ["ruf=mailto:${var.dmarc_ruf_email}"] : [],
    ["sp=${var.dmarc_policy}", "adkim=s", "aspf=s"]
  ))
}

# ====================================================================
# SES 域名驗證 TXT 記錄
# ====================================================================

resource "cloudflare_record" "ses_domain_verification" {
  count = local.domain_verification != null ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = replace(local.domain_verification.name, ".${var.domain_name}", "")
  content = local.domain_verification.content
  type    = local.domain_verification.type
  proxied = false  # 驗證記錄不能透過 Cloudflare Proxy
  ttl     = local.domain_verification.ttl
  comment = local.domain_verification.comment
}

# ====================================================================
# DKIM CNAME 記錄 (3 組)
# ====================================================================

resource "cloudflare_record" "ses_dkim" {
  count = length(local.dkim_records)

  zone_id = var.cloudflare_zone_id
  name    = replace(local.dkim_records[count.index].name, ".${var.domain_name}", "")
  content = local.dkim_records[count.index].content
  type    = local.dkim_records[count.index].type
  proxied = false  # DKIM 記錄不能透過 Cloudflare Proxy
  ttl     = local.dkim_records[count.index].ttl
  comment = local.dkim_records[count.index].comment
}

# ====================================================================
# MAIL FROM MX 記錄
# ====================================================================

resource "cloudflare_record" "ses_mail_from_mx" {
  count = local.mail_from_mx != null ? 1 : 0

  zone_id  = var.cloudflare_zone_id
  name     = replace(local.mail_from_mx.name, ".${var.domain_name}", "")
  content  = local.mail_from_mx.content
  type     = local.mail_from_mx.type
  priority = local.mail_from_mx.priority
  proxied  = false  # MX 記錄不能透過 Cloudflare Proxy
  ttl      = local.mail_from_mx.ttl
  comment  = local.mail_from_mx.comment
}

# ====================================================================
# MAIL FROM SPF TXT 記錄
# ====================================================================

resource "cloudflare_record" "ses_mail_from_spf" {
  count = local.mail_from_spf != null ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = replace(local.mail_from_spf.name, ".${var.domain_name}", "")
  content = local.mail_from_spf.content
  type    = local.mail_from_spf.type
  proxied = false  # SPF 記錄不能透過 Cloudflare Proxy
  ttl     = local.mail_from_spf.ttl
  comment = local.mail_from_spf.comment
}

# ====================================================================
# DMARC TXT 記錄
# ====================================================================

resource "cloudflare_record" "dmarc" {
  count = local.ses_deployed ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = "_dmarc"
  content = local.dmarc_record
  type    = "TXT"
  proxied = false
  ttl     = 1800
  comment = "DMARC 郵件驗證記錄"
}
