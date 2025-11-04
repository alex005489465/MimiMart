# ====================================================================
# IAM 政策定義
# ====================================================================
# 用途: 定義後端存取 S3 所需的 IAM 政策
# 注意: 此模組不會建立 IAM User/Role,僅輸出政策 JSON 供手動建立使用
# ====================================================================

# --------------------------------------------------------------------
# IAM 政策: 後端 S3 存取權限
#
# 權限範圍:
#   - Member Bucket: 完整 CRUD 權限
#   - Public Bucket: 完整 CRUD 權限
#
# 遵循最小權限原則,僅授予必要的操作權限
# --------------------------------------------------------------------

locals {
  # IAM 政策 JSON
  backend_iam_policy = {
    Version = "2012-10-17"
    Statement = [
      # Member Bucket: 物件層級操作
      {
        Sid    = "MemberBucketObjectAccess"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:GetObjectVersion",
          "s3:GetObjectAttributes"
        ]
        Resource = "${aws_s3_bucket.member.arn}/*"
      },
      # Member Bucket: Bucket 層級操作
      {
        Sid    = "MemberBucketListAccess"
        Effect = "Allow"
        Action = [
          "s3:ListBucket",
          "s3:GetBucketLocation",
          "s3:GetBucketVersioning"
        ]
        Resource = aws_s3_bucket.member.arn
      },
      # Public Bucket: 物件層級操作
      {
        Sid    = "PublicBucketObjectAccess"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:GetObjectVersion",
          "s3:GetObjectAttributes"
        ]
        Resource = "${aws_s3_bucket.public.arn}/*"
      },
      # Public Bucket: Bucket 層級操作
      {
        Sid    = "PublicBucketListAccess"
        Effect = "Allow"
        Action = [
          "s3:ListBucket",
          "s3:GetBucketLocation",
          "s3:GetBucketVersioning"
        ]
        Resource = aws_s3_bucket.public.arn
      }
    ]
  }
}
