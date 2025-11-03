# ====================================================================
# Terraform 輸出
# ====================================================================

# --------------------------------------------------------------------
# SES 域名身份資訊
# --------------------------------------------------------------------

output "ses_domain_identity_arn" {
  description = "SES 域名身份的 ARN"
  value       = aws_ses_domain_identity.main.arn
}

output "ses_domain_identity_verification_token" {
  description = "SES 域名驗證 Token (用於建立 DNS TXT 記錄)"
  value       = aws_ses_domain_identity.main.verification_token
}

# --------------------------------------------------------------------
# DKIM 簽章資訊
# --------------------------------------------------------------------

output "ses_dkim_tokens" {
  description = "DKIM 驗證 Tokens (用於建立 DNS CNAME 記錄，共 3 組)"
  value       = aws_ses_domain_dkim.main.dkim_tokens
}

# --------------------------------------------------------------------
# MAIL FROM 域名資訊
# --------------------------------------------------------------------

output "ses_mail_from_domain" {
  description = "MAIL FROM 域名 (例如: noreply.example.com)"
  value       = var.enable_mail_from_domain ? aws_ses_domain_mail_from.main[0].mail_from_domain : null
}

output "ses_mail_from_mx_record" {
  description = "MAIL FROM 域名的 MX 記錄值 (用於 DNS 設定)"
  value       = var.enable_mail_from_domain ? "feedback-smtp.${var.aws_region}.amazonses.com" : null
}

output "ses_mail_from_spf_record" {
  description = "MAIL FROM 域名的 SPF TXT 記錄值 (用於 DNS 設定)"
  value       = var.enable_mail_from_domain ? "v=spf1 include:amazonses.com ~all" : null
}

# --------------------------------------------------------------------
# IAM 政策 JSON (供手動建立使用)
# --------------------------------------------------------------------

output "iam_policy_json" {
  description = "IAM 政策 JSON (複製到 AWS Console 建立 inline policy)"
  value       = data.aws_iam_policy_document.ses_send.json
}

output "iam_policy_pretty" {
  description = "IAM 政策格式化輸出 (易於閱讀)"
  value       = jsondecode(data.aws_iam_policy_document.ses_send.json)
}

# --------------------------------------------------------------------
# Configuration Set 資訊
# --------------------------------------------------------------------

output "ses_configuration_set_name" {
  description = "SES Configuration Set 名稱"
  value       = aws_ses_configuration_set.main.name
}

output "ses_configuration_set_arn" {
  description = "SES Configuration Set 的 ARN"
  value       = aws_ses_configuration_set.main.arn
}

# --------------------------------------------------------------------
# SMTP 連線資訊
# --------------------------------------------------------------------

output "smtp_endpoint" {
  description = "SES SMTP 端點"
  value       = "email-smtp.${var.aws_region}.amazonses.com"
}

output "smtp_port" {
  description = "SES SMTP 連接埠 (587 for STARTTLS, 465 for TLS Wrapper)"
  value       = "587"
}

# --------------------------------------------------------------------
# 後端環境變數
# --------------------------------------------------------------------

output "backend_environment_variables" {
  description = "後端應用程式所需的環境變數"
  value = {
    MAIL_HOST           = "email-smtp.${var.aws_region}.amazonses.com"
    MAIL_PORT           = "587"
    MAIL_USERNAME       = "[在 AWS Console 建立 IAM 使用者的 Access Key ID]"
    MAIL_PASSWORD       = "[使用 AWS 提供的 SES SMTP Password 轉換工具]"
    MAIL_SMTP_AUTH      = "true"
    MAIL_SMTP_STARTTLS  = "true"
    MAIL_FROM_ADDRESS   = "noreply@${var.domain_name}"
    MAIL_FROM_NAME      = var.project_name
  }
  sensitive = false
}

output "smtp_credential_guide" {
  description = "SMTP 憑證取得指引"
  value = {
    step_1 = "前往 AWS IAM Console 建立使用者"
    step_2 = "使用者名稱建議: ${var.project_name}-${var.environment}-ses-smtp-user"
    step_3 = "建立 inline policy，使用 'iam_policy_json' 輸出的內容"
    step_4 = "建立 Access Key (Application running outside AWS)"
    step_5 = "使用 Access Key 產生 SMTP 密碼"
    smtp_password_tool = "https://docs.aws.amazon.com/ses/latest/dg/smtp-credentials.html#smtp-credentials-convert"
    note = "⚠️ SMTP Password 與 Secret Access Key 不同，需要使用 AWS 提供的轉換工具或腳本"
  }
}

# --------------------------------------------------------------------
# DNS 記錄需求 (供 Cloudflare DNS 模組使用)
# --------------------------------------------------------------------

output "dns_records" {
  description = "需要建立的 DNS 記錄清單 (供 Cloudflare DNS 模組自動讀取)"
  value = {
    # 域名驗證 TXT 記錄
    domain_verification = {
      name    = "_amazonses.${var.domain_name}"
      type    = "TXT"
      content = aws_ses_domain_identity.main.verification_token
      ttl     = 1800
      comment = "SES 域名驗證記錄"
    }

    # DKIM CNAME 記錄 (3 組)
    dkim_records = [
      for token in aws_ses_domain_dkim.main.dkim_tokens : {
        name    = "${token}._domainkey.${var.domain_name}"
        type    = "CNAME"
        content = "${token}.dkim.amazonses.com"
        ttl     = 1800
        comment = "SES DKIM 驗證記錄"
      }
    ]

    # MAIL FROM MX 記錄
    mail_from_mx = var.enable_mail_from_domain ? {
      name     = var.mail_from_subdomain
      type     = "MX"
      content  = "feedback-smtp.${var.aws_region}.amazonses.com"
      priority = 10
      ttl      = 1800
      comment  = "SES MAIL FROM MX 記錄"
    } : null

    # MAIL FROM SPF TXT 記錄
    mail_from_spf = var.enable_mail_from_domain ? {
      name    = var.mail_from_subdomain
      type    = "TXT"
      content = "v=spf1 include:amazonses.com ~all"
      ttl     = 1800
      comment = "SES MAIL FROM SPF 記錄"
    } : null
  }
}

# --------------------------------------------------------------------
# 部署摘要
# --------------------------------------------------------------------

output "deployment_summary" {
  description = "部署摘要與後續步驟"
  value = {
    ses_domain = {
      domain                = var.domain_name
      mail_from_domain      = var.enable_mail_from_domain ? var.mail_from_subdomain : "使用預設 (amazonses.com)"
      smtp_endpoint         = "email-smtp.${var.aws_region}.amazonses.com"
      smtp_port             = "587 (STARTTLS)"
      configuration_set     = aws_ses_configuration_set.main.name
      event_destination     = var.enable_event_destination ? "已啟用 (SNS)" : "未啟用"
    }

    verification_status = {
      domain_verification = "待驗證 - 需要建立 DNS TXT 記錄"
      dkim_verification   = "待驗證 - 需要建立 DNS CNAME 記錄 (3 組)"
      mail_from_verification = var.enable_mail_from_domain ? "待驗證 - 需要建立 DNS MX 和 TXT 記錄" : "未啟用"
    }

    environment = var.environment

    next_steps = [
      "1. 查看 IAM 政策 JSON:",
      "   terraform output iam_policy_json",
      "   # 或查看檔案: IAM_POLICY.json",
      "",
      "2. 在 AWS Console 建立 IAM 使用者:",
      "   a. 前往 IAM → Users → Create user",
      "   b. 使用者名稱: ${var.project_name}-${var.environment}-ses-smtp-user",
      "   c. 建立 inline policy，貼上步驟 1 的 JSON",
      "   d. 建立 Access Key (選擇: Application running outside AWS)",
      "   e. 將 Access Key ID 轉換為 SMTP Password",
      "      工具: https://docs.aws.amazon.com/ses/latest/dg/smtp-credentials.html#smtp-credentials-convert",
      "",
      "3. 部署 Cloudflare DNS-SES 模組 (自動建立所需的 DNS 記錄):",
      "   cd ../../cloudflare/dns-ses",
      "   terraform init",
      "   terraform apply",
      "",
      "4. 驗證 DNS 記錄是否已生效 (通常需要 10-30 分鐘):",
      "   # Windows PowerShell",
      "   nslookup -type=TXT _amazonses.${var.domain_name}",
      "   nslookup -type=CNAME [dkim-token]._domainkey.${var.domain_name}",
      var.enable_mail_from_domain ? "   nslookup -type=MX ${var.mail_from_subdomain}" : "",
      "",
      "5. 檢查 SES 域名驗證狀態:",
      "   # 在 AWS Console 查看: SES → Verified identities → ${var.domain_name}",
      "   # 或使用 AWS CLI:",
      "   aws ses get-identity-verification-attributes --identities ${var.domain_name}",
      "",
      "6. 申請脫離沙盒模式 (生產環境):",
      "   # 在 AWS Console: SES → Account dashboard → Request production access",
      "   # 需要說明:",
      "   # - 使用案例: 會員註冊驗證、密碼重設郵件",
      "   # - 如何處理退信: 透過 SNS 事件追蹤並記錄到資料庫",
      "   # - 郵件清單維護: 僅發送給註冊會員,不進行行銷郵件",
      "   # 審核時間: 通常 1-2 個工作日",
      "",
      "7. 更新後端環境變數 (生產環境):",
      "   # 編輯 infra/prod-env/aws-ec2/backend/.env",
      "   # 使用 'backend_environment_variables' 輸出的值",
      "",
      "8. 測試郵件發送:",
      "   # 沙盒模式下只能發送到已驗證的郵箱",
      "   # 在 AWS Console 先驗證測試郵箱: SES → Verified identities → Create identity",
      "",
      "9. 設定 DMARC (選用但建議):",
      "   # 在 Cloudflare DNS 新增 TXT 記錄:",
      "   # _dmarc.${var.domain_name} → v=DMARC1; p=quarantine; rua=mailto:postmaster@${var.domain_name}"
    ]
  }
}
