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
      name    = aws_s3_bucket.member.bucket
      purpose = "Member avatar storage (PRIVATE)"
      access  = "Backend API only - S3 is completely private"
    }
    environment = var.environment
    next_steps = [
      "1. Copy the IAM policy JSON from 'backend_iam_policy_json' output",
      "2. Go to AWS Console → IAM → Users → Create User",
      "3. Create user named 'mimimart-dev-backend-user'",
      "4. Attach the copied policy as an inline policy",
      "5. Create Access Key and save credentials securely",
      "6. Configure backend with environment variables from 'backend_environment_variables' output",
      "7. Implement backend API endpoints:",
      "   - POST /api/shop/member/avatar/upload (accept multipart file)",
      "   - GET /api/shop/member/avatar/{memberId} (return image stream)",
      "8. Test file upload and access through backend API"
    ]
  }
}
