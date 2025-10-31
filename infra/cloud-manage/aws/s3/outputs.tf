# ====================================================================
# Terraform 輸出
# ====================================================================

# --------------------------------------------------------------------
# Member Bucket 資訊
# --------------------------------------------------------------------

output "member_bucket_id" {
  description = "Member bucket ID"
  value       = aws_s3_bucket.member.id
}

output "member_bucket_arn" {
  description = "Member bucket ARN"
  value       = aws_s3_bucket.member.arn
}

output "member_bucket_name" {
  description = "Member bucket name"
  value       = aws_s3_bucket.member.bucket
}

output "member_bucket_region" {
  description = "Member bucket region"
  value       = aws_s3_bucket.member.region
}

# --------------------------------------------------------------------
# Public Bucket 資訊
# --------------------------------------------------------------------

output "public_bucket_id" {
  description = "Public bucket ID"
  value       = aws_s3_bucket.public.id
}

output "public_bucket_arn" {
  description = "Public bucket ARN"
  value       = aws_s3_bucket.public.arn
}

output "public_bucket_name" {
  description = "Public bucket name"
  value       = aws_s3_bucket.public.bucket
}

output "public_bucket_region" {
  description = "Public bucket region"
  value       = aws_s3_bucket.public.region
}

output "public_bucket_website_endpoint" {
  description = "Public bucket website endpoint (for Cloudflare DNS)"
  value       = aws_s3_bucket_website_configuration.public.website_endpoint
}

# --------------------------------------------------------------------
# IAM 政策 JSON
# --------------------------------------------------------------------

output "backend_iam_policy_json" {
  description = "IAM policy JSON for backend S3 access (copy this to create IAM user/role policy)"
  value       = jsonencode(local.backend_iam_policy)
}

output "backend_iam_policy_pretty" {
  description = "IAM policy in pretty-printed format for easy reading"
  value       = local.backend_iam_policy
}

# --------------------------------------------------------------------
# 整合資訊
# --------------------------------------------------------------------

output "backend_integration_guide" {
  description = "Backend API integration guide"
  value = {
    note                = "S3 is private. All access must go through backend API."
    upload_endpoint     = "POST /api/shop/member/avatar/upload (multipart/form-data)"
    read_endpoint       = "GET /api/shop/member/avatar/{memberId}"
    cloudflare_dns      = "NOT NEEDED - Direct API access only"
    cloudflare_waf      = "OPTIONAL - Can protect backend API endpoints"
    backend_permissions = "Backend needs S3 GetObject and PutObject permissions (see IAM policy output)"
  }
}

output "backend_environment_variables" {
  description = "Environment variables needed for backend application"
  value = {
    AWS_REGION            = aws_s3_bucket.member.region
    AWS_S3_MEMBER_BUCKET  = aws_s3_bucket.member.bucket
    AWS_S3_PUBLIC_BUCKET  = aws_s3_bucket.public.bucket
    AWS_ACCESS_KEY_ID     = "[MANUALLY_CREATED_IAM_USER_ACCESS_KEY]"
    AWS_SECRET_ACCESS_KEY = "[MANUALLY_CREATED_IAM_USER_SECRET_KEY]"
  }
  sensitive = false
}

# --------------------------------------------------------------------
# 部署摘要
# --------------------------------------------------------------------

output "deployment_summary" {
  description = "Deployment summary and next steps"
  value = {
    member_bucket = {
      name     = aws_s3_bucket.member.bucket
      purpose  = "Member avatar storage (PRIVATE)"
      access   = "Backend API only - S3 is completely private"
      endpoint = "N/A - Not publicly accessible"
    }
    public_bucket = {
      name     = aws_s3_bucket.public.bucket
      purpose  = "Public resources storage (IP-RESTRICTED)"
      access   = "Cloudflare CDN + Developer IPs only"
      endpoint = aws_s3_bucket_website_configuration.public.website_endpoint
      cdn_url  = "http://shop-storage-public-dev.xenolume.com (configured via Cloudflare DNS)"
    }
    environment = var.environment
    next_steps = [
      "1. Setup developer IPs:",
      "   cd modules/ip-ranges",
      "   cp developer-ips.json.example developer-ips.json",
      "   # Edit developer-ips.json with your actual IP",
      "",
      "2. Deploy Cloudflare DNS records:",
      "   cd cloudflare/dns",
      "   terraform init",
      "   terraform apply",
      "",
      "3. Copy the IAM policy JSON from 'backend_iam_policy_json' output",
      "",
      "4. Create IAM User in AWS Console:",
      "   - Go to AWS Console → IAM → Users → Create User",
      "   - User name: 'mimimart-dev-backend-user'",
      "   - Attach the copied policy as an inline policy",
      "   - Create Access Key and save credentials securely",
      "",
      "5. Configure backend with environment variables from 'backend_environment_variables' output",
      "",
      "6. Test access:",
      "   - Member bucket: Through backend API only",
      "   - Public bucket: Via Cloudflare CDN or direct (with allowed IP)"
    ]
  }
}
