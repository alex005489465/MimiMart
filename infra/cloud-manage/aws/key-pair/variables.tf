# AWS 認證資訊
variable "aws_region" {
  description = "AWS 區域"
  type        = string
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

# 專案資訊
variable "project_name" {
  description = "專案名稱"
  type        = string
}

variable "environment" {
  description = "環境名稱"
  type        = string
}

# Key Pair 配置
variable "key_name" {
  description = "Key Pair 名稱"
  type        = string
}

variable "public_key" {
  description = "SSH 公鑰內容（需提供完整的公鑰字串）"
  type        = string
}

variable "additional_tags" {
  description = "額外的標籤"
  type        = map(string)
  default     = {}
}
