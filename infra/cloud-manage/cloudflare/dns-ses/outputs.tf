# ====================================================================
# Terraform 輸出
# ====================================================================

# --------------------------------------------------------------------
# SES DNS 記錄資訊
# --------------------------------------------------------------------

output "ses_dns_records_status" {
  description = "SES DNS 記錄建立狀態"
  value = {
    ses_deployed = local.ses_deployed

    domain_verification = local.domain_verification != null ? {
      created  = true
      name     = local.domain_verification.name
      type     = local.domain_verification.type
      content  = local.domain_verification.content
    } : {
      created = false
      message = "SES 模組尚未部署或未產生驗證 Token"
    }

    dkim_records = length(local.dkim_records) > 0 ? [
      for idx, record in local.dkim_records : {
        index   = idx + 1
        name    = record.name
        type    = record.type
        content = record.content
      }
    ] : []

    mail_from_mx = local.mail_from_mx != null ? {
      created  = true
      name     = local.mail_from_mx.name
      type     = local.mail_from_mx.type
      content  = local.mail_from_mx.content
      priority = local.mail_from_mx.priority
    } : {
      created = false
      message = "MAIL FROM 域名未啟用"
    }

    mail_from_spf = local.mail_from_spf != null ? {
      created = true
      name    = local.mail_from_spf.name
      type    = local.mail_from_spf.type
      content = local.mail_from_spf.content
    } : {
      created = false
      message = "MAIL FROM 域名未啟用"
    }

    dmarc = local.ses_deployed ? {
      created = true
      name    = "_dmarc.${var.domain_name}"
      type    = "TXT"
      content = local.dmarc_record
      policy  = var.dmarc_policy
    } : {
      created = false
      message = "SES 模組尚未部署"
    }
  }
}

# --------------------------------------------------------------------
# 部署摘要
# --------------------------------------------------------------------

output "deployment_summary" {
  description = "DNS 記錄部署摘要"
  value = {
    total_records_created = (
      (local.domain_verification != null ? 1 : 0) +
      length(local.dkim_records) +
      (local.mail_from_mx != null ? 1 : 0) +
      (local.mail_from_spf != null ? 1 : 0) +
      (local.ses_deployed ? 1 : 0)
    )

    records_breakdown = {
      domain_verification = local.domain_verification != null ? 1 : 0
      dkim_records        = length(local.dkim_records)
      mail_from_mx        = local.mail_from_mx != null ? 1 : 0
      mail_from_spf       = local.mail_from_spf != null ? 1 : 0
      dmarc               = local.ses_deployed ? 1 : 0
    }

    verification_status = {
      note = "DNS 記錄已建立，請等待 10-30 分鐘讓 DNS 傳播"
      next_steps = [
        "1. 等待 DNS 記錄生效（通常 10-30 分鐘）",
        "",
        "2. 驗證 DNS 記錄（Windows PowerShell）:",
        "   nslookup -type=TXT _amazonses.${var.domain_name}",
        local.mail_from_mx != null ? "   nslookup -type=MX ${replace(local.mail_from_mx.name, ".${var.domain_name}", "")}.${var.domain_name}" : "",
        "",
        "3. 檢查 AWS SES 驗證狀態:",
        "   # AWS Console → SES → Verified identities → ${var.domain_name}",
        "   # 或使用 AWS CLI:",
        "   aws ses get-identity-verification-attributes --identities ${var.domain_name}",
        "",
        "4. 驗證通過後，即可開始發送郵件",
        "   # 沙盒模式下需先驗證收件人郵箱",
        "   # 生產模式需要申請脫離沙盒（詳見 SES 模組 README）"
      ]
    }

    troubleshooting = {
      dns_not_propagated = "如果驗證失敗，請等待更長時間（最多 72 小時）並檢查 DNS 記錄是否正確"
      check_cloudflare   = "在 Cloudflare Dashboard 檢查 DNS 記錄是否已建立"
      ses_not_deployed   = local.ses_deployed ? "SES 模組已部署" : "請先部署 AWS SES 模組"
    }
  }
}

# --------------------------------------------------------------------
# DNS 驗證指令
# --------------------------------------------------------------------

output "verification_commands" {
  description = "DNS 記錄驗證指令（Windows PowerShell）"
  value = {
    domain_verification = local.domain_verification != null ? "nslookup -type=TXT ${local.domain_verification.name}" : "N/A"

    dkim_records = length(local.dkim_records) > 0 ? [
      for record in local.dkim_records :
      "nslookup -type=CNAME ${record.name}"
    ] : []

    mail_from_mx  = local.mail_from_mx != null ? "nslookup -type=MX ${local.mail_from_mx.name}" : "N/A"
    mail_from_spf = local.mail_from_spf != null ? "nslookup -type=TXT ${local.mail_from_spf.name}" : "N/A"
    dmarc         = local.ses_deployed ? "nslookup -type=TXT _dmarc.${var.domain_name}" : "N/A"
  }
}
