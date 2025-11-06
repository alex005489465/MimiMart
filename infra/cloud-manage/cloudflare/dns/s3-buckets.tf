# ====================================================================
# S3 Buckets DNS 記錄自動化
# ====================================================================
# 用途: 自動建立 S3 bucket 的 Cloudflare DNS CNAME 記錄
# 方式: 透過 terraform_remote_state 讀取 S3 模組的輸出
# ====================================================================

# --------------------------------------------------------------------
# 讀取 S3 模組的 State
# --------------------------------------------------------------------

data "terraform_remote_state" "s3" {
  backend = "local"

  config = {
    path = "../../aws/s3/terraform.tfstate"
  }
}

# --------------------------------------------------------------------
# S3 Public Bucket DNS 記錄
# --------------------------------------------------------------------

resource "cloudflare_record" "s3_public" {
  # 只有當 S3 state 存在且 public_bucket_subdomain 不為空時才建立
  count = try(data.terraform_remote_state.s3.outputs.public_bucket_name, null) != null && var.public_bucket_subdomain != "" ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = var.public_bucket_subdomain
  # 使用 S3 REST API endpoint (支援 HTTPS)
  content = "${data.terraform_remote_state.s3.outputs.public_bucket_name}.s3.${data.terraform_remote_state.s3.outputs.public_bucket_region}.amazonaws.com"
  type    = "CNAME"
  proxied = true
  ttl     = 1
  comment = "Auto-managed: S3 public bucket CDN endpoint (HTTPS supported)"
}

# --------------------------------------------------------------------
# 輸出資訊
# --------------------------------------------------------------------

output "s3_dns_records" {
  description = "S3 buckets DNS records information"
  value = {
    public_bucket = {
      enabled      = try(data.terraform_remote_state.s3.outputs.public_bucket_name, null) != null && var.public_bucket_subdomain != ""
      dns_name     = var.public_bucket_subdomain != "" ? "${var.public_bucket_subdomain}.${var.domain_name}" : "N/A - Not configured"
      s3_endpoint  = try("${data.terraform_remote_state.s3.outputs.public_bucket_name}.s3.${data.terraform_remote_state.s3.outputs.public_bucket_region}.amazonaws.com", "N/A - S3 not deployed yet")
      proxied      = true
      protocol     = "HTTPS"
      access_note  = "IP-restricted to Cloudflare + Developer IPs"
    }
  }
}
