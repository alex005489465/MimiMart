# ============================================================================
# Cloudflare 認證資訊
# ============================================================================

variable "cloudflare_api_token" {
  description = "Cloudflare API Token (需要 Zone:Cloud Connector:Write 權限)"
  type        = string
  sensitive   = true
}

variable "cloudflare_zone_id" {
  description = "Cloudflare Zone ID"
  type        = string
}

# ============================================================================
# Cloud Connector 配置 - 多環境支援
# ============================================================================

variable "environments" {
  description = "Cloud Connector 環境配置列表 (支援多個環境同時存在)"
  type = list(object({
    description = string
    enabled     = bool
    expression  = string
    s3_host     = string
  }))

  validation {
    condition     = length(var.environments) > 0
    error_message = "至少需要配置一個環境"
  }

  validation {
    condition = alltrue([
      for env in var.environments :
      can(regex("^[a-z0-9.-]+\\.s3\\.[a-z0-9-]+\\.amazonaws\\.com$", env.s3_host))
    ])
    error_message = "所有 s3_host 格式必須為: bucket-name.s3.region.amazonaws.com"
  }

  validation {
    condition = alltrue([
      for env in var.environments :
      length(env.expression) > 0 && length(env.description) > 0
    ])
    error_message = "expression 和 description 不能為空"
  }
}
