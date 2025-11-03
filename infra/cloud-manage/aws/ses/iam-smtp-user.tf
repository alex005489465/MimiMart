# ====================================================================
# IAM 政策定義 (供手動建立使用)
# ====================================================================
# 用途: 提供 IAM 政策範本供手動在 AWS Console 建立
#
# 使用步驟:
#   1. 在 AWS Console 手動建立 IAM 使用者
#   2. 建立 Access Key 並產生 SMTP 憑證
#   3. 將 IAM_POLICY.json 內容附加為 inline policy
#
# 說明:
#   - 此檔案僅用於生成政策範本輸出
#   - 不會自動建立任何 IAM 資源
#   - IAM 資源需要手動在 AWS Console 建立
# ====================================================================

# IAM 政策文件 - 定義 SES 發送權限
data "aws_iam_policy_document" "ses_send" {
  # 允許發送郵件 (從已驗證的身份)
  statement {
    sid    = "AllowSendEmail"
    effect = "Allow"

    actions = [
      "ses:SendEmail",
      "ses:SendRawEmail"
    ]

    resources = [
      aws_ses_domain_identity.main.arn,
      "${aws_ses_domain_identity.main.arn}/*"
    ]
  }

  # 允許使用 Configuration Set (用於郵件追蹤與統計)
  statement {
    sid    = "AllowUseConfigurationSet"
    effect = "Allow"

    actions = [
      "ses:PutConfigurationSetDeliveryOptions",
      "ses:GetConfigurationSet"
    ]

    resources = [
      aws_ses_configuration_set.main.arn
    ]
  }
}
