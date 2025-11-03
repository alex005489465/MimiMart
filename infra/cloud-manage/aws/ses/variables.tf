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
  description = "AWS region where SES resources will be created"
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
# SES 域名配置
# ====================================================================

variable "domain_name" {
  description = "主域名用於 SES 驗證 (例如: example.com)"
  type        = string
}

# ====================================================================
# MAIL FROM 域名配置
# ====================================================================

variable "enable_mail_from_domain" {
  description = "是否啟用自訂 MAIL FROM 域名"
  type        = bool
  default     = true
}

variable "mail_from_subdomain" {
  description = "MAIL FROM 子域名 (例如: noreply.example.com)"
  type        = string
  default     = ""
}

variable "mail_from_behavior_on_mx_failure" {
  description = "當 MAIL FROM 域名 MX 記錄驗證失敗時的行為 (UseDefaultValue 或 RejectMessage)"
  type        = string
  default     = "UseDefaultValue"

  validation {
    condition     = contains(["UseDefaultValue", "RejectMessage"], var.mail_from_behavior_on_mx_failure)
    error_message = "mail_from_behavior_on_mx_failure 必須是 UseDefaultValue 或 RejectMessage"
  }
}

# ====================================================================
# Configuration Set 配置
# ====================================================================

variable "enable_event_destination" {
  description = "是否啟用事件目的地 (SNS Topic) 用於接收 bounce/complaint/delivery 事件"
  type        = bool
  default     = false
}

# ====================================================================
# 標籤配置
# ====================================================================

variable "additional_tags" {
  description = "Additional tags to apply to all resources"
  type        = map(string)
  default     = {}
}
