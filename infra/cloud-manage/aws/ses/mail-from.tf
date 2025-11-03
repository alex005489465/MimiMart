# ====================================================================
# MAIL FROM 域名設定
# ====================================================================
# 用途: 自訂郵件發送來源域名,提升郵件信譽與合規性
#
# 說明:
#   - MAIL FROM 域名會出現在郵件標頭的 Return-Path 欄位
#   - 建議使用子域名,如 noreply.example.com
#   - 需要在 DNS 中設定 MX 和 SPF TXT 記錄
#
# DNS 記錄需求:
#   - MX: noreply.example.com -> feedback-smtp.ap-south-1.amazonses.com
#   - TXT: noreply.example.com -> v=spf1 include:amazonses.com ~all
# ====================================================================

# 設定 MAIL FROM 域名
resource "aws_ses_domain_mail_from" "main" {
  count = var.enable_mail_from_domain ? 1 : 0

  domain           = aws_ses_domain_identity.main.domain
  mail_from_domain = var.mail_from_subdomain

  # 行為設定: 當 MAIL FROM 域名驗證失敗時的處理方式
  # UseDefaultValue: 使用預設的 amazonses.com 域名
  # RejectMessage: 拒絕發送郵件
  behavior_on_mx_failure = var.mail_from_behavior_on_mx_failure
}
