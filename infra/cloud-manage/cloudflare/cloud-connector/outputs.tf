# ============================================================================
# Cloud Connector 輸出
# ============================================================================

output "cloud_connector_id" {
  description = "Cloud Connector resource ID"
  value       = cloudflare_cloud_connector_rules.s3_public.id
}

output "cloud_connector_environments" {
  description = "所有已配置的環境"
  value = {
    for env in var.environments :
    regex("http\\.host eq \"(.+?)\"", env.expression)[0] => {
      enabled     = env.enabled
      target_host = env.s3_host
      description = env.description
    }
  }
}

output "cloud_connector_summary" {
  description = "Cloud Connector 配置摘要"
  value = {
    total_rules = length(var.environments)
    environments = [
      for env in var.environments : {
        cdn_domain  = regex("http\\.host eq \"(.+?)\"", env.expression)[0]
        s3_bucket   = split(".", env.s3_host)[0]
        enabled     = env.enabled
      }
    ]
  }
}
