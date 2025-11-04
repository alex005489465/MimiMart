# Cloudflare DNS - S3 生產環境

## 目標

為生產環境的 S3 Public Bucket 建立 Cloudflare DNS CNAME 記錄，啟用 CDN 和 SSL。

## 前置條件

1. `aws/s3-prod` 模組已部署（需要讀取其 terraform.tfstate）
2. 已完成 `../cloudflare/.env` 配置（Cloudflare 認證資訊）
3. Docker 和 Docker Compose 已安裝

## 執行步驟

### 1. 配置變數

複製範例配置並編輯：

```bash
cp terraform.tfvars.example terraform.tfvars
```

需要的資訊：
- `public_bucket_subdomain`：子域名（例如：cdn、storage）

### 2. 初始化 Terraform

在 `infra/cloud-manage` 目錄執行：

```bash
docker-compose --env-file cloudflare/.env run --rm terraform "cd cloudflare/dns-s3-prod && terraform init"
```

### 3. 檢查執行計畫

```bash
docker-compose --env-file cloudflare/.env run --rm terraform "cd cloudflare/dns-s3-prod && terraform plan"
```

### 4. 部署資源

```bash
docker-compose --env-file cloudflare/.env run --rm terraform "cd cloudflare/dns-s3-prod && terraform apply"
```

### 5. 查看輸出

```bash
docker-compose --env-file cloudflare/.env run --rm terraform "cd cloudflare/dns-s3-prod && terraform output"
```

## 驗證

檢查 DNS 記錄和 HTTPS 存取：

```bash
# 查看 CDN URL
terraform output s3_public_cdn_full_url

# 測試 DNS 解析
nslookup <subdomain>.<your-domain>

# 測試 HTTPS 存取
curl -I https://<subdomain>.<your-domain>
```

## 完成

DNS 記錄已建立，S3 Public Bucket 可透過 Cloudflare CDN 存取。
