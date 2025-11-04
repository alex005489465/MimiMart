# ====================================================================
# S3 儲存桶模組 - 生產環境
# ====================================================================
# 用途: 為後端應用提供檔案儲存服務
#
# 包含資源:
#   - Member Bucket: 會員大頭照 (私有)
#   - Public Bucket: 商品圖片等公開資源 (IP 限制)
#   - IAM 政策: 供手動建立 IAM User 使用的政策定義
#
# 部署順序: 無依賴,可獨立部署
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
      Module      = "S3"
    },
    var.additional_tags
  )
}
