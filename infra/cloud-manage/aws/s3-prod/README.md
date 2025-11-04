# AWS S3 儲存桶 - 生產環境

## 目標

建立並配置生產環境的 S3 儲存桶，提供會員頭像和公開資源的物件儲存服務。

## 模組說明

此模組建立兩個 S3 儲存桶：

- **Member Bucket**：存放會員頭像，完全私有，僅透過後端 API 存取
- **Public Bucket**：存放公開靜態資源，透過 Cloudflare CDN 存取，限制來源 IP

## 前置條件

1. 已完成 `../aws/.env` 配置（AWS 認證資訊）
2. Docker 和 Docker Compose 已安裝
3. 已確定 S3 bucket 名稱（必須全球唯一）

## 執行步驟

### 1. 配置變數

複製範例配置並編輯：

```bash
cp terraform.tfvars.example terraform.tfvars
```

必填變數：
- `project_name`：專案名稱
- `environment`：環境名稱（prod）
- `member_bucket_name`：Member bucket 名稱（全球唯一）
- `public_bucket_name`：Public bucket 名稱（全球唯一）
- `member_cors_allowed_origins`：Member bucket 允許的 CORS 來源
- `public_cors_allowed_origins`：Public bucket 允許的 CORS 來源
- `public_bucket_cdn_domain`：Cloudflare CDN 網域

### 2. 初始化 Terraform

在 `infra/cloud-manage` 目錄執行：

```bash
docker-compose --env-file aws/.env run --rm terraform "cd aws/s3-prod && terraform init"
```

### 3. 檢查執行計畫

```bash
docker-compose --env-file aws/.env run --rm terraform "cd aws/s3-prod && terraform plan"
```

### 4. 部署資源

```bash
docker-compose --env-file aws/.env run --rm terraform "cd aws/s3-prod && terraform apply"
```

### 5. 取得 IAM 政策

複製輸出的 IAM 政策 JSON：

```bash
docker-compose --env-file aws/.env run --rm terraform "cd aws/s3-prod && terraform output -raw backend_iam_policy_json"
```

### 6. 建立 IAM User

在 AWS Console 手動建立：

1. 前往 IAM → Users → Create user
2. 使用者名稱：`<project>-<env>-backend-user`
3. 不啟用 Console 存取
4. 建立政策並貼上步驟 5 的 JSON
5. 附加政策到使用者
6. 建立 Access Key 並儲存憑證

### 7. 配置 Cloudflare DNS

使用 `public_bucket_website_endpoint` 輸出建立 CNAME 記錄：

```
Type: CNAME
Name: <subdomain>
Target: <public_bucket_website_endpoint>
Proxy: Enabled
```

### 8. 配置後端環境變數

參考 `backend_environment_variables` 輸出設定後端的環境變數。

## 驗證

檢查部署摘要：

```bash
docker-compose --env-file aws/.env run --rm terraform "cd aws/s3-prod && terraform output deployment_summary"
```

測試上傳（需要配置 IAM 憑證）：

```bash
aws s3 cp test.jpg s3://<bucket-name>/test.jpg
```

## 完成

S3 儲存桶已部署，後續需要配置 Cloudflare DNS 和後端環境變數。
