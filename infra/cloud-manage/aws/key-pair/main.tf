# ====================================================================
# Key Pair 模組
# ====================================================================
# 用途: 管理 EC2 實例的 SSH 金鑰對
#
# 包含資源:
#   - AWS Key Pair: 上傳公鑰至 AWS，供 EC2 實例使用
#
# 部署順序: 無依賴，可獨立部署（EC2 模組的前置需求）
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
      Module      = "KeyPair"
    },
    var.additional_tags
  )
}

# ====================================================================
# Key Pair 資源
# ====================================================================

resource "aws_key_pair" "main" {
  key_name   = var.key_name
  public_key = var.public_key

  tags = merge(
    local.common_tags,
    {
      Name = var.key_name
    }
  )
}
