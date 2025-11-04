# ====================================================================
# S3 Bucket DNS 記錄 (生產環境)
# ====================================================================
# 自動讀取生產環境 S3 模組的 state,建立對應的 DNS 記錄
# ====================================================================

# --------------------------------------------------------------------
# Remote State Data Source
# --------------------------------------------------------------------

data "terraform_remote_state" "s3_prod" {
  backend = "local"

  config = {
    path = "../../aws/s3-prod/terraform.tfstate"
  }
}

# --------------------------------------------------------------------
# Public Bucket DNS 記錄
# --------------------------------------------------------------------

resource "cloudflare_record" "s3_public_prod" {
  # 只有當 S3 已部署且子域名不為空時才建立
  count = (
    try(data.terraform_remote_state.s3_prod.outputs.public_bucket_website_endpoint, null) != null
    && var.public_bucket_subdomain != ""
  ) ? 1 : 0

  zone_id = var.cloudflare_zone_id
  name    = var.public_bucket_subdomain
  content = data.terraform_remote_state.s3_prod.outputs.public_bucket_website_endpoint
  type    = "CNAME"
  proxied = true
  ttl     = 1
  comment = "Production - S3 public bucket CDN endpoint (Cloudflare IP-restricted)"
}
