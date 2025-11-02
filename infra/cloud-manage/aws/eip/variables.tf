# ========================================
# AWS 認證資訊
# ========================================

variable "aws_region" {
  description = "AWS 區域"
  type        = string

  validation {
    condition     = can(regex("^[a-z]{2}-[a-z]+-[0-9]$", var.aws_region))
    error_message = "AWS 區域格式不正確，例如：us-east-1、ap-northeast-1"
  }
}

variable "aws_access_key" {
  description = "AWS Access Key ID"
  type        = string
  sensitive   = true
}

variable "aws_secret_key" {
  description = "AWS Secret Access Key"
  type        = string
  sensitive   = true
}

# ========================================
# 專案資訊
# ========================================

variable "project_name" {
  description = "專案名稱，用於資源命名"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.project_name))
    error_message = "專案名稱只能包含小寫字母、數字和連字號"
  }
}

variable "environment" {
  description = "環境名稱 (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "環境名稱必須是 dev, staging 或 prod"
  }
}

# ========================================
# EIP 配置
# ========================================

variable "eip_count" {
  description = "要建立的 Elastic IP 數量"
  type        = number

  validation {
    condition     = var.eip_count > 0 && var.eip_count <= 10
    error_message = "EIP 數量必須在 1 到 10 之間"
  }
}

# ========================================
# 額外標籤
# ========================================

variable "additional_tags" {
  description = "額外的標籤"
  type        = map(string)
  default     = {}
}
