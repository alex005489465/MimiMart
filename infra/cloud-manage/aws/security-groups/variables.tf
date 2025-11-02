# ============================================================
# AWS 認證變數
# ============================================================
# 這些變數應從 aws/.env 檔案透過環境變數載入 (TF_VAR_*)
# 請勿在 terraform.tfvars 中填寫這些敏感資訊

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

variable "aws_region" {
  description = "AWS 區域"
  type        = string
}

# ============================================================
# 專案基本資訊
# ============================================================

variable "project_name" {
  description = "專案名稱,用於資源命名"
  type        = string
}

variable "environment" {
  description = "環境名稱 (dev/staging/prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "環境名稱必須是 dev, staging 或 prod"
  }
}

# ============================================================
# VPC 配置 (依賴項)
# ============================================================

variable "vpc_id" {
  description = "VPC ID,從 VPC 模組輸出取得"
  type        = string

  validation {
    condition     = can(regex("^vpc-[a-z0-9]+$", var.vpc_id))
    error_message = "VPC ID 格式錯誤,應為 vpc-xxxxxxxxx 格式"
  }
}

# ============================================================
# 標籤配置
# ============================================================

variable "additional_tags" {
  description = "額外的自訂標籤"
  type        = map(string)
  default     = {}
}
