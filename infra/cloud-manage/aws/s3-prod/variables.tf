# ====================================================================
# AWS Provider 認證變數
# ====================================================================

variable "aws_access_key" {
  description = "AWS Access Key ID for authentication"
  type        = string
  sensitive   = true
}

variable "aws_secret_key" {
  description = "AWS Secret Access Key for authentication"
  type        = string
  sensitive   = true
}

variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
}

# ====================================================================
# 專案基本資訊
# ====================================================================

variable "project_name" {
  description = "Project name used for resource naming and tagging"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g., dev, staging, prod)"
  type        = string
}

# ====================================================================
# Member Bucket 配置
# ====================================================================

variable "member_bucket_name" {
  description = "Name for the member avatar bucket (must be globally unique)"
  type        = string
}

variable "member_enable_versioning" {
  description = "Enable versioning for member bucket (false for dev environment to save cost)"
  type        = bool
}

variable "member_cors_allowed_origins" {
  description = "List of allowed origins for CORS (e.g., ['http://localhost:5173', 'http://cdn.example.com'])"
  type        = list(string)
}

variable "member_cors_allowed_methods" {
  description = "List of allowed HTTP methods for CORS"
  type        = list(string)
}

variable "member_cors_allowed_headers" {
  description = "List of allowed headers for CORS"
  type        = list(string)
}

variable "member_cors_max_age_seconds" {
  description = "Max age for CORS preflight cache in seconds"
  type        = number
}

# ====================================================================
# Public Bucket 配置
# ====================================================================

variable "public_bucket_name" {
  description = "Name for the public bucket (must be globally unique)"
  type        = string
}

variable "public_enable_versioning" {
  description = "Enable versioning for public bucket (false for dev environment to save cost)"
  type        = bool
}

variable "public_cors_allowed_origins" {
  description = "List of allowed origins for CORS (e.g., ['http://localhost:5173', 'http://cdn.example.com'])"
  type        = list(string)
}

variable "public_cors_allowed_methods" {
  description = "List of allowed HTTP methods for CORS"
  type        = list(string)
}

variable "public_cors_allowed_headers" {
  description = "List of allowed headers for CORS"
  type        = list(string)
}

variable "public_cors_max_age_seconds" {
  description = "Max age for CORS preflight cache in seconds"
  type        = number
}

# ====================================================================
# 標籤配置
# ====================================================================

variable "additional_tags" {
  description = "Additional tags to apply to all resources"
  type        = map(string)
  default     = {}
}

# ====================================================================
# CDN 配置
# ====================================================================

variable "public_bucket_cdn_domain" {
  description = "Public bucket 的 Cloudflare CDN 完整域名（例如: storage.example.com）"
  type        = string
}
