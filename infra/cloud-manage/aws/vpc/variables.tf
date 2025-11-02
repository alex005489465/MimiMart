# AWS 認證變數（從 .env 讀取 TF_VAR_* 環境變數）
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
  description = "AWS Region"
  type        = string
  default     = "ap-south-1"
}

# 專案標籤變數
variable "project_name" {
  description = "專案名稱，用於資源命名"
  type        = string
  default     = "myproject"
}

variable "environment" {
  description = "環境標籤（dev/staging/prod）"
  type        = string
  default     = "dev"
}

# VPC 配置變數
variable "vpc_cidr" {
  description = "VPC 的 CIDR 區塊"
  type        = string
  default     = "10.0.0.0/16"
}

# Public Subnet 配置
variable "public_subnet_cidr" {
  description = "Public Subnet 的 CIDR 區塊"
  type        = string
  default     = "10.0.1.0/24"
}

variable "availability_zone" {
  description = "可用區（Availability Zone）"
  type        = string
  default     = "ap-south-1a"
}
