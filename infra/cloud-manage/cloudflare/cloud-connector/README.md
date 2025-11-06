# Cloudflare Cloud Connector 配置

## 目標

配置 Cloudflare Cloud Connector 規則,自動將特定域名的請求路由到 AWS S3 並改寫 Host header。

## 前置條件

- 已建立 Cloudflare Zone 並取得 Zone ID
- 已產生具備 `Zone:Cloud Connector:Write` 權限的 Cloudflare API Token
- 已在 AWS S3 建立 bucket 並配置公開存取

## 執行步驟

### 1. 複製配置範例

```bash
cp terraform.tfvars.example terraform.tfvars
```

### 2. 填寫配置檔案

編輯 `terraform.tfvars`,設定各環境的 Cloud Connector 規則:

需要的資訊:
- `description`: 規則描述
- `expression`: Cloudflare 規則表達式(例如: `(http.host eq "cdn.example.com")`)
- `s3_host`: AWS S3 bucket host(格式: `bucket-name.s3.region.amazonaws.com`)

### 3. 設定環境變數

在執行 Terraform 前,需設定以下環境變數:

```bash
export TF_VAR_cloudflare_api_token=<your-api-token>
export TF_VAR_cloudflare_zone_id=<your-zone-id>
```

### 4. 執行 Terraform

```bash
terraform init
terraform plan
terraform apply
```

## 驗證

前往 Cloudflare Dashboard > 對應 Zone > Cloud Connector,檢查規則是否已建立並啟用。

## 完成

Cloud Connector 規則已配置完成,指定域名的請求將自動路由到 AWS S3。
