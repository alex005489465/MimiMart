# S3 儲存桶模組

為 shop-backend 生產環境提供物件儲存服務。

## 📋 模組說明

此模組建立並管理三個 S3 儲存桶:

### 1. Static Bucket (通用靜態資源)
- **用途**: 存放通用靜態資源
- **存取方式**: 通過 Cloudflare CDN 公開讀取
- **後端權限**: 完整 CRUD (建立、讀取、更新、刪除)
- **安全性**: 啟用 SSE-S3 伺服器端加密
- **版本控制**: 可選啟用
- **CORS**: 支援跨域存取配置

### 2. Products Bucket (商品圖片)
- **用途**: 專門存放商品圖片
- **存取方式**: 通過 Cloudflare CDN 公開讀取
- **後端權限**: 完整 CRUD
- **安全性**: 啟用 SSE-S3 伺服器端加密
- **版本控制**: 可選啟用
- **CORS**: 支援跨域存取配置

### 3. Log Bucket (應用程式日誌)
- **用途**: 存放應用程式日誌
- **存取方式**: 完全私有,僅後端可存取
- **後端權限**: 完整 CRUD
- **安全性**: 啟用 SSE-S3 伺服器端加密,阻止所有公開存取
- **生命週期**: 自動刪除超過指定天數的舊日誌
- **版本控制**: 可選啟用

## 🏗️ 架構設計

```
┌─────────────────────────────────────────────────────────────┐
│                      使用者/客戶端                            │
└─────────────┬───────────────────────┬───────────────────────┘
              │                       │
              ▼                       ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ Cloudflare CDN  │     │ Cloudflare CDN  │
    │ (assets)        │     │ (images)        │
    │ - SSL/TLS       │     │ - SSL/TLS       │
    │ - 全球快取      │     │ - 全球快取      │
    └────────┬────────┘     └────────┬────────┘
             │                       │
             ▼                       ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ S3 Static       │     │ S3 Products     │
    │ - 靜態網站託管  │     │ - 靜態網站託管  │
    │ - 公開讀取      │     │ - 公開讀取      │
    │ - SSE-S3 加密   │     │ - SSE-S3 加密   │
    └────────▲────────┘     └────────▲────────┘
             │ CRUD                  │ CRUD
             └───────────┬───────────┘
                         │
              ┌──────────────────┐
              │  shop-backend    │
              │  (IAM User 認證) │
              └──────────┬───────┘
                         │ CRUD
                         ▼
              ┌──────────────────┐
              │  S3 Log Bucket   │
              │  - 完全私有      │
              │  - 生命週期管理  │
              │  - SSE-S3 加密   │
              └──────────────────┘
```

## 📦 建立的資源

### Static Bucket
- `aws_s3_bucket.static` - S3 儲存桶
- `aws_s3_bucket_versioning.static` - 版本控制配置
- `aws_s3_bucket_server_side_encryption_configuration.static` - 加密配置
- `aws_s3_bucket_website_configuration.static` - 靜態網站託管配置
- `aws_s3_bucket_public_access_block.static` - 公開存取配置
- `aws_s3_bucket_policy.static_public_read` - Bucket 政策 (公開讀取)
- `aws_s3_bucket_cors_configuration.static` - CORS 配置

### Log Bucket
- `aws_s3_bucket.log` - S3 儲存桶
- `aws_s3_bucket_versioning.log` - 版本控制配置
- `aws_s3_bucket_server_side_encryption_configuration.log` - 加密配置
- `aws_s3_bucket_public_access_block.log` - 阻止公開存取
- `aws_s3_bucket_lifecycle_configuration.log` - 生命週期管理

## 🔧 部署步驟

### 前置條件

1. 已完成 `aws/.env` 配置 (AWS 認證資訊)
2. Docker 和 Docker Compose 已安裝
3. 已確定 S3 bucket 名稱 (必須全球唯一)

### 步驟 1: 配置變數

```bash
# 複製範例配置
cp terraform.tfvars.example terraform.tfvars

# 編輯配置檔案,替換所有佔位符
# 注意: AWS 認證資訊從 aws/.env 自動載入,無需在此設定
```

**必填變數**:
```hcl
project_name           = "mimimart"
environment            = "prod"
static_bucket_name     = "mimimart-prod-static"
log_bucket_name        = "mimimart-prod-logs"
log_retention_days     = 90
static_enable_versioning = true
log_enable_versioning  = false

static_cors_allowed_origins = [
  "https://mimimart.com",
  "https://www.mimimart.com"
]
static_cors_allowed_methods = ["GET", "HEAD", "PUT", "POST", "DELETE"]
static_cors_allowed_headers = ["*"]
static_cors_max_age_seconds = 3600
```

### 步驟 2: Terraform 操作

> **注意**：所有指令需要在 `infra/cloud-manage` 目錄下執行

```bash
# 通用指令模板
docker-compose --env-file aws/.env run --rm terraform "cd s3 && terraform <command>"
```

**重要輸出**:
- `static_bucket_website_endpoint` - 用於 Cloudflare CNAME 記錄
- `backend_s3_policy_json` - IAM 政策 JSON (複製此內容)
- `deployment_summary` - 完整部署摘要

### 步驟 3: 建立 IAM User

Terraform 不會自動建立 IAM User,需要手動操作:

1. **複製 IAM 政策**
   ```bash
   # 輸出格式化的政策 JSON
   docker-compose --env-file aws/.env run --rm terraform "cd s3 && terraform output -raw backend_s3_policy_json"
   ```

2. **前往 AWS Console**
   - 導航至 `IAM` → `Users` → `Create user`
   - 使用者名稱: `shop-backend-prod-user` (或自訂)
   - 不選擇 AWS Management Console access

3. **附加政策**
   - 選擇 `Attach policies directly`
   - 點擊 `Create policy`
   - 選擇 `JSON` 標籤
   - 貼上剛才複製的政策 JSON
   - 政策名稱: `ShopBackendS3Access`
   - 建立政策後返回使用者建立頁面並附加此政策

4. **建立 Access Key**
   - 建立使用者後,進入使用者詳情頁
   - 點擊 `Security credentials` 標籤
   - 點擊 `Create access key`
   - 選擇 `Application running outside AWS`
   - 複製 `Access key ID` 和 `Secret access key`
   - **重要**: 立即保存這些憑證,離開頁面後將無法再次查看

### 步驟 7: 配置 Cloudflare

1. **建立 DNS 記錄**
   - 登入 Cloudflare Dashboard
   - 選擇你的網域
   - 前往 `DNS` → `Records`
   - 點擊 `Add record`

   ```
   Type: CNAME
   Name: cdn (或 static)
   Target: [從 static_bucket_website_endpoint 輸出取得]
   Proxy status: Proxied (橘色雲朵,啟用)
   TTL: Auto
   ```

2. **設定 SSL/TLS**
   - 前往 `SSL/TLS` → `Overview`
   - 選擇 `Flexible` 或 `Full`

3. **優化快取設定 (可選)**
   - 前往 `Rules` → `Page Rules` 或 `Cache Rules`
   - 為商品圖片設定長時間快取

   ```
   URL: cdn.yourdomain.com/products/*
   Cache Level: Cache Everything
   Edge Cache TTL: 1 month
   Browser Cache TTL: 4 hours
   ```

4. **防盜鏈保護 (可選)**
   - 前往 `Rules` → `Transform Rules`
   - 建立規則阻擋 Referer 不是你網域的請求

### 步驟 8: 配置後端環境變數

在 shop-backend 的環境變數中設定:

```bash
# AWS 基本配置
AWS_REGION=ap-northeast-1
AWS_ACCESS_KEY_ID=[步驟 6 建立的 Access Key ID]
AWS_SECRET_ACCESS_KEY=[步驟 6 建立的 Secret Access Key]

# S3 Bucket 名稱
AWS_S3_STATIC_BUCKET=mimimart-prod-static
AWS_S3_LOG_BUCKET=mimimart-prod-logs

# Cloudflare CDN URL (用於產生公開圖片連結)
S3_STATIC_CDN_URL=https://cdn.mimimart.com
```

### 步驟 9: 測試功能

1. **測試上傳**
   ```bash
   # 使用 AWS CLI 測試 (需要配置 IAM User 憑證)
   aws s3 cp test-image.jpg s3://mimimart-prod-static/products/test-image.jpg
   ```

2. **測試存取**
   ```bash
   # 直接從 S3 存取
   curl http://[static_bucket_website_endpoint]/products/test-image.jpg

   # 從 Cloudflare CDN 存取
   curl https://cdn.mimimart.com/products/test-image.jpg
   ```

3. **測試日誌上傳**
   ```bash
   echo "Test log" | aws s3 cp - s3://mimimart-prod-logs/test.log
   aws s3 ls s3://mimimart-prod-logs/
   ```

## 📊 變數說明

### 必填變數

| 變數名稱 | 類型 | 說明 | 範例 |
|---------|------|------|------|
| `aws_access_key` | string | AWS Access Key (從 .env 載入) | - |
| `aws_secret_key` | string | AWS Secret Key (從 .env 載入) | - |
| `aws_region` | string | AWS 區域 | `ap-northeast-1` |
| `project_name` | string | 專案名稱 | `mimimart` |
| `environment` | string | 環境名稱 | `prod` |
| `static_bucket_name` | string | Static bucket 名稱 (全球唯一) | `mimimart-prod-static` |
| `log_bucket_name` | string | Log bucket 名稱 (全球唯一) | `mimimart-prod-logs` |
| `static_enable_versioning` | bool | Static bucket 版本控制 | `true` |
| `static_cors_allowed_origins` | list(string) | CORS 允許的來源 | `["https://mimimart.com"]` |
| `static_cors_allowed_methods` | list(string) | CORS 允許的方法 | `["GET", "PUT", "POST", "DELETE"]` |
| `static_cors_allowed_headers` | list(string) | CORS 允許的標頭 | `["*"]` |
| `static_cors_max_age_seconds` | number | CORS 預檢快取時間 | `3600` |
| `log_retention_days` | number | 日誌保留天數 | `90` |
| `log_enable_versioning` | bool | Log bucket 版本控制 | `false` |

### 可選變數

| 變數名稱 | 類型 | 說明 | 預設值 |
|---------|------|------|--------|
| `additional_tags` | map(string) | 額外的資源標籤 | `{}` |

## 📤 輸出說明

| 輸出名稱 | 說明 | 用途 |
|---------|------|------|
| `static_bucket_name` | Static bucket 名稱 | 後端配置 |
| `static_bucket_arn` | Static bucket ARN | 其他 AWS 資源整合 |
| `static_bucket_website_endpoint` | S3 website endpoint | Cloudflare CNAME 目標 |
| `log_bucket_name` | Log bucket 名稱 | 後端配置 |
| `log_bucket_arn` | Log bucket ARN | 其他 AWS 資源整合 |
| `backend_s3_policy_json` | IAM 政策 JSON | 手動建立 IAM User |
| `cloudflare_integration_guide` | Cloudflare 整合指南 | DNS 配置參考 |
| `backend_environment_variables` | 後端環境變數範本 | 後端配置參考 |
| `deployment_summary` | 部署摘要和後續步驟 | 完整操作指南 |

## 🔒 安全性說明

### 資料加密
- 所有 bucket 都啟用 SSE-S3 (AES-256) 伺服器端加密
- 傳輸中加密: 建議只使用 HTTPS 存取

### 存取控制
- **Static Bucket**: 公開讀取,但建議僅透過 Cloudflare CDN 存取
- **Log Bucket**: 完全私有,阻止所有公開存取
- **IAM 政策**: 遵循最小權限原則,僅授予必要操作

### Cloudflare 保護層
- DDoS 防護
- SSL/TLS 加密
- 可選的 WAF (Web Application Firewall)
- 可選的防盜鏈保護
- 可選的地理位置限制

### 最佳實務
1. **憑證管理**
   - 妥善保存 IAM Access Key
   - 定期輪換金鑰
   - 不要將金鑰提交至版本控制

2. **監控**
   - 啟用 CloudTrail 記錄 API 呼叫
   - 設定 CloudWatch 警報監控異常存取
   - 定期檢查 S3 存取日誌

3. **備份**
   - 啟用 Static bucket 版本控制
   - 定期備份重要靜態資源
   - 考慮使用 S3 Cross-Region Replication

## 💰 成本估算

### Static Bucket
- **儲存成本**: ~$0.023 per GB/月 (標準儲存)
- **請求成本**: PUT/POST $0.005 per 1000 requests, GET $0.0004 per 1000 requests
- **流量成本**: 透過 Cloudflare CDN,大部分流量被快取,降低 S3 流量成本

### Log Bucket
- **儲存成本**: ~$0.023 per GB/月 (90 天後自動刪除)
- **請求成本**: PUT/POST $0.005 per 1000 requests

### 節省成本技巧
1. 啟用生命週期管理自動刪除舊資料
2. 使用 Cloudflare CDN 減少 S3 請求次數
3. 考慮使用 S3 Intelligent-Tiering 自動最佳化儲存類別
4. 設定 CloudWatch 警報監控成本

## 🔧 維護與管理

### 更新配置

修改 `terraform.tfvars` 後,執行 `terraform plan` 和 `terraform apply`。

### 查看 Bucket 內容

```bash
# Static bucket
aws s3 ls s3://mimimart-prod-static/ --recursive

# Log bucket
aws s3 ls s3://mimimart-prod-logs/ --recursive
```

### 清理舊日誌 (手動)

```bash
# 列出超過 90 天的日誌
aws s3 ls s3://mimimart-prod-logs/ --recursive | \
  awk '$1 < "'$(date -d '90 days ago' +%Y-%m-%d)'" {print $4}'

# 注意: 生命週期規則會自動處理,通常不需要手動清理
```

### 銷毀資源

⚠️ **警告**: 這將刪除所有 bucket 和內容!確保已備份重要資料。

先使用 AWS CLI 清空 bucket (必要步驟,否則 destroy 會失敗),再執行 `terraform destroy`。

## ❓ 常見問題

### Q1: 為什麼不由 Terraform 建立 IAM User?

**A**: 基於安全性考量:
- IAM Access Key 是高度敏感資訊
- Terraform state 檔案會記錄明文金鑰
- 手動建立可確保金鑰只在建立時顯示一次
- 更符合最小權限和職責分離原則

### Q2: 可以改用 IAM Role 而不是 IAM User 嗎?

**A**: 可以!如果後端運行在 EC2 上:
- 建立 IAM Role 並附加相同的政策
- 將 Role 附加到 EC2 Instance Profile
- 後端應用不需要配置 Access Key
- 更安全,AWS SDK 會自動取得臨時憑證

### Q3: Static bucket 設為公開安全嗎?

**A**: 在以下情況下是安全的:
- 內容本身是公開商品圖片,不包含敏感資訊
- 透過 Cloudflare CDN 存取,享有 DDoS 保護
- 可在 Cloudflare 設定額外的防盜鏈和 WAF 規則
- 如果需要更高安全性,參考「Bucket Policy 限制 Cloudflare IP」方案

### Q4: 如何處理大量歷史日誌?

**A**: 可以調整生命週期規則:
- 較舊的日誌轉移到 Glacier (更便宜的儲存)
- 或直接刪除超過保留期限的日誌
- 修改 `log_retention_days` 變數並重新 apply

### Q5: 商品圖片需要調整尺寸怎麼辦?

**A**: 有幾種方案:
1. **後端處理**: 上傳前在後端生成多種尺寸
2. **Cloudflare Images**: 使用 Cloudflare 的圖片優化服務 (付費)
3. **Lambda@Edge**: 在 CloudFront 邊緣動態調整 (需要整合 CloudFront)
4. **Cloudflare Workers**: 在邊緣動態處理圖片

### Q6: 如何監控 S3 使用量?

**A**: 可以:
- 啟用 S3 Storage Lens 查看使用趨勢
- 設定 CloudWatch 警報監控儲存大小
- 查看 AWS Cost Explorer 追蹤成本
- 啟用 S3 存取日誌記錄

## 📚 相關文件

- [AWS S3 官方文件](https://docs.aws.amazon.com/s3/)
- [S3 靜態網站託管](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html)
- [Cloudflare CDN 文件](https://developers.cloudflare.com/cache/)
- [AWS IAM 最佳實務](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)
- [S3 定價說明](https://aws.amazon.com/s3/pricing/)

## 🆘 支援

如果遇到問題:
1. 檢查 Terraform 錯誤訊息
2. 確認 AWS 認證資訊正確
3. 確認 S3 bucket 名稱全球唯一
4. 查看 AWS CloudTrail 日誌
5. 參考相關文件連結

## 📝 變更記錄

- **2025-01-XX**: 初始版本
  - 建立 Static 和 Log 兩個 bucket
  - 整合 Cloudflare CDN
  - 手動 IAM User 建立流程
  - 完整文件和範例
