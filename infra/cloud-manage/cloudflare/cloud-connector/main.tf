terraform {
  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 5.5.0"
    }
  }
  required_version = ">= 1.0"
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# ====================================================================
# Cloudflare Cloud Connector 規則
# ====================================================================
# 用途: 配置 Cloud Connector 自動處理 S3 Host header 改寫
# 功能: 將特定域名的請求路由到 AWS S3,並自動設定正確的 Host header
# 支援: 多環境配置 (dev, prod 同時存在)
# ====================================================================

resource "cloudflare_cloud_connector_rules" "s3_public" {
  zone_id = var.cloudflare_zone_id

  # 包含所有環境的規則列表
  rules = [
    for env in var.environments : {
      description = env.description
      enabled     = env.enabled
      expression  = env.expression
      provider    = "aws_s3"
      parameters = {
        host = env.s3_host
      }
    }
  ]
}
