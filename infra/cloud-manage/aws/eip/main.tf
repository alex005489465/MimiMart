# ========================================
# Terraform 和 Provider 配置
# ========================================

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
  region     = var.aws_region
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
}

# ========================================
# Elastic IP
# ========================================

resource "aws_eip" "main" {
  count  = var.eip_count
  domain = "vpc"

  tags = merge(
    {
      Name        = "${var.project_name}-${var.environment}-eip-${count.index + 1}"
      Environment = var.environment
      Project     = var.project_name
    },
    var.additional_tags
  )
}
