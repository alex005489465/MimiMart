# ============================================================
# Terraform 與 Provider 配置
# ============================================================

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
  region     = var.aws_region
}

# ============================================================
# IP 範圍模組引用
# ============================================================

module "ip_ranges" {
  source = "../../modules/ip-ranges"
}

# ============================================================
# 共用標籤配置
# ============================================================

locals {
  common_tags = merge(
    {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Module      = "SecurityGroups"
    },
    var.additional_tags
  )
}
