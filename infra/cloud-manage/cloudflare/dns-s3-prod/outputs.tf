# ====================================================================
# Outputs - 生產環境 S3 DNS
# ====================================================================

output "s3_public_cdn_url" {
  description = "生產環境 S3 Public Bucket CDN URL (完整域名)"
  value       = try(cloudflare_record.s3_public_prod[0].hostname, "尚未建立 DNS 記錄")
}

output "s3_public_cdn_full_url" {
  description = "生產環境 S3 Public Bucket CDN URL (含 HTTPS)"
  value       = try("https://${cloudflare_record.s3_public_prod[0].hostname}", "尚未建立 DNS 記錄")
}

output "s3_bucket_website_endpoint" {
  description = "S3 Website Endpoint (來自 S3 模組)"
  value       = try(data.terraform_remote_state.s3_prod.outputs.public_bucket_website_endpoint, "S3 尚未部署")
}
