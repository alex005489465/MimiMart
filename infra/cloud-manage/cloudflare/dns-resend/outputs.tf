# ====================================================================
# Terraform 輸出
# ====================================================================

output "resend_dns_records" {
  description = "Resend DNS 記錄狀態"
  value = {
    dkim = {
      name    = cloudflare_record.resend_dkim.hostname
      type    = cloudflare_record.resend_dkim.type
      content = cloudflare_record.resend_dkim.content
      status  = "已建立"
    }
    mx = {
      name     = cloudflare_record.resend_mx.hostname
      type     = cloudflare_record.resend_mx.type
      content  = cloudflare_record.resend_mx.content
      priority = cloudflare_record.resend_mx.priority
      status   = "已建立"
    }
    spf = {
      name    = cloudflare_record.resend_spf.hostname
      type    = cloudflare_record.resend_spf.type
      content = cloudflare_record.resend_spf.content
      status  = "已建立"
    }
    dmarc = var.enable_dmarc ? {
      name    = cloudflare_record.resend_dmarc[0].hostname
      type    = cloudflare_record.resend_dmarc[0].type
      content = cloudflare_record.resend_dmarc[0].content
      status  = "已建立"
    } : {
      status = "未啟用"
    }
  }
}

output "verification_commands" {
  description = "DNS 驗證指令"
  value = {
    dkim  = "nslookup -type=TXT ${cloudflare_record.resend_dkim.hostname}"
    mx    = "nslookup -type=MX ${cloudflare_record.resend_mx.hostname}"
    spf   = "nslookup -type=TXT ${cloudflare_record.resend_spf.hostname}"
    dmarc = var.enable_dmarc ? "nslookup -type=TXT ${cloudflare_record.resend_dmarc[0].hostname}" : "N/A"
  }
}

output "email_configuration" {
  description = "後端郵件配置資訊"
  value = {
    email_domain    = cloudflare_record.resend_spf.hostname
    from_address    = "noreply@${cloudflare_record.resend_spf.hostname}"
    provider        = "Resend"
    resend_api_url  = "https://api.resend.com"
  }
}

output "next_steps" {
  description = "後續步驟"
  value = [
    "1. 等待 DNS 記錄生效（通常 1-5 分鐘）",
    "",
    "2. 在 Resend Dashboard 檢查域名驗證狀態:",
    "   https://resend.com/domains",
    "",
    "3. 取得 Resend API Key:",
    "   Dashboard → API Keys → Create API Key",
    "",
    "4. 更新後端環境變數:",
    "   EMAIL_PROVIDER=resend",
    "   RESEND_API_KEY=re_xxxxx",
    "   MAIL_FROM_ADDRESS=noreply@${cloudflare_record.resend_spf.hostname}",
    "   MAIL_FROM_NAME=YourAppName",
    "",
    "5. 測試發送郵件"
  ]
}
