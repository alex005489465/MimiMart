# ====================================================================
# SES 電子郵件服務模組
# ====================================================================
# 用途: 為後端應用提供生產環境郵件發送服務
#
# 包含資源:
#   - Domain Identity: 域名身份驗證 (example.com)
#   - DKIM Tokens: DKIM 簽章驗證 (3 組 tokens)
#   - Configuration Set: 郵件追蹤與事件設定
#
# 部署順序: 需要先部署,再執行 Cloudflare DNS 模組設定驗證記錄
# ====================================================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# ====================================================================
# AWS Provider 配置
# ====================================================================

provider "aws" {
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
  region     = var.aws_region
}

# ====================================================================
# 共用的 Locals
# ====================================================================

locals {
  # 標準標籤
  common_tags = merge(
    {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Module      = "SES"
    },
    var.additional_tags
  )
}

# ====================================================================
# SES 域名身份驗證
# ====================================================================

# 建立域名身份
resource "aws_ses_domain_identity" "main" {
  domain = var.domain_name
}

# 啟用 DKIM 簽章
resource "aws_ses_domain_dkim" "main" {
  domain = aws_ses_domain_identity.main.domain
}

# ====================================================================
# Configuration Set - 郵件追蹤與事件
# ====================================================================

# 建立配置集
resource "aws_ses_configuration_set" "main" {
  name = "${var.project_name}-${var.environment}-ses-config"

  # 啟用信譽儀表板
  reputation_metrics_enabled = true

  # 啟用發送統計
  sending_enabled = true
}

# SNS 主題 - 用於接收 SES 事件
resource "aws_sns_topic" "ses_events" {
  count = var.enable_event_destination ? 1 : 0
  name  = "${var.project_name}-${var.environment}-ses-events"

  tags = local.common_tags
}

# 事件目的地 - Bounce (退信)
resource "aws_ses_event_destination" "bounce" {
  count                  = var.enable_event_destination ? 1 : 0
  name                   = "bounce-destination"
  configuration_set_name = aws_ses_configuration_set.main.name
  enabled                = true
  matching_types         = ["bounce"]

  sns_destination {
    topic_arn = aws_sns_topic.ses_events[0].arn
  }
}

# 事件目的地 - Complaint (投訴)
resource "aws_ses_event_destination" "complaint" {
  count                  = var.enable_event_destination ? 1 : 0
  name                   = "complaint-destination"
  configuration_set_name = aws_ses_configuration_set.main.name
  enabled                = true
  matching_types         = ["complaint"]

  sns_destination {
    topic_arn = aws_sns_topic.ses_events[0].arn
  }
}

# 事件目的地 - Delivery (成功送達)
resource "aws_ses_event_destination" "delivery" {
  count                  = var.enable_event_destination ? 1 : 0
  name                   = "delivery-destination"
  configuration_set_name = aws_ses_configuration_set.main.name
  enabled                = true
  matching_types         = ["delivery"]

  sns_destination {
    topic_arn = aws_sns_topic.ses_events[0].arn
  }
}
