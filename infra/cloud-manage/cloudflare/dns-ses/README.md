# Cloudflare DNS-SES 自動化部署

## 目標

自動建立 AWS SES 所需的 Cloudflare DNS 驗證記錄，包括域名驗證、DKIM 簽章、MAIL FROM 配置。

## 前置條件

1. 已部署 AWS SES 模組
2. 已配置 Cloudflare 認證資訊
3. 使用與 SES 模組相同的域名
4. Docker 和 Docker Compose 已安裝

## 執行步驟

### 1. 確認 SES 模組已部署

檢查 SES state 檔案和輸出：

```bash
# 檢查 SES 模組輸出
cd ../../aws/ses
terraform output dns_records
cd ../../cloudflare/dns-ses
```

如果 SES 模組尚未部署，請先完成 SES 模組部署。

### 2. 配置變數

複製範例檔案並編輯：

```bash
cp terraform.tfvars.example terraform.tfvars
```

配置內容（必須與 SES 模組一致）：

```hcl
domain_name = "example.com"
```

### 3. 初始化並部署

在 `cloud-manage` 目錄執行：

```bash
# 初始化 Terraform
docker-compose --env-file cloudflare/.env run --rm terraform "cd dns-ses && terraform init"

# 預覽變更
docker-compose --env-file cloudflare/.env run --rm terraform "cd dns-ses && terraform plan"

# 部署（預期建立 6 筆 DNS 記錄）
docker-compose --env-file cloudflare/.env run --rm terraform "cd dns-ses && terraform apply"
```

預期建立的記錄：
- 1 個域名驗證 TXT 記錄
- 3 個 DKIM CNAME 記錄
- 1 個 MAIL FROM MX 記錄
- 1 個 MAIL FROM SPF TXT 記錄

### 4. 查看部署摘要

```bash
# 查看 DNS 記錄狀態
docker-compose --env-file cloudflare/.env run --rm terraform "cd dns-ses && terraform output ses_dns_records_status"

# 查看驗證指令
docker-compose --env-file cloudflare/.env run --rm terraform "cd dns-ses && terraform output verification_commands"
```

### 5. 驗證 DNS 記錄

等待 DNS 傳播（10-30 分鐘）後驗證：

```bash
# 域名驗證記錄
nslookup -type=TXT _amazonses.example.com

# DKIM 記錄（替換 [token] 為實際值）
nslookup -type=CNAME [token]._domainkey.example.com

# MAIL FROM MX 記錄
nslookup -type=MX noreply.example.com

# MAIL FROM SPF 記錄
nslookup -type=TXT noreply.example.com
```

### 6. 檢查 AWS SES 驗證狀態

DNS 記錄生效後，AWS SES 會自動驗證：

```bash
# 使用 AWS CLI
aws ses get-identity-verification-attributes --identities example.com

# 或在 AWS Console 查看
# SES → Verified identities → example.com
```

驗證成功的狀態：
- Domain verification status: Success
- DKIM verification status: Success
- Mail From domain verification status: Success

## 驗證

部署成功後：
- 6 筆 DNS 記錄已建立在 Cloudflare
- DNS 記錄傳播完成（可透過 nslookup 查詢）
- AWS SES 域名狀態顯示 "Verified"
- 所有驗證項目（Domain、DKIM、MAIL FROM）通過

## 完成

Cloudflare DNS 記錄已建立，AWS SES 域名驗證完成，可開始發送郵件。
