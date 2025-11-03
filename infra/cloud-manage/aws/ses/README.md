# AWS SES 郵件服務部署

## 目標

部署 AWS SES (Simple Email Service) 用於生產環境的郵件發送，包含域名驗證、DKIM 簽章與 SMTP 認證。

## 前置條件

1. 已配置 AWS 認證資訊（需要 SES 相關權限）
2. 擁有域名管理權限
3. Docker 和 Docker Compose 已安裝
4. 已部署 Cloudflare DNS 模組（用於自動建立 DNS 記錄）

## 執行步驟

### 1. 配置變數

複製範例檔案並編輯配置：

```bash
cp terraform.tfvars.example terraform.tfvars
```

最少需要配置的變數：

```hcl
project_name            = "your-project"
environment             = "prod"
domain_name             = "example.com"
mail_from_subdomain     = "noreply.example.com"
```

### 2. 初始化並部署

在 `cloud-manage` 目錄執行：

```bash
# 初始化 Terraform
docker-compose --env-file aws/.env run --rm terraform "cd ses && terraform init"

# 預覽變更
docker-compose --env-file aws/.env run --rm terraform "cd ses && terraform plan"

# 部署
docker-compose --env-file aws/.env run --rm terraform "cd ses && terraform apply"
```

### 3. 建立 IAM 使用者與 SMTP 憑證

查看 IAM 政策並手動建立使用者：

```bash
# 查看 IAM 政策
docker-compose --env-file aws/.env run --rm terraform "cd ses && terraform output iam_policy_json"
```

**手動建立步驟：**

1. 登入 AWS IAM Console
2. 建立使用者（不需要 Console 存取）
3. 附加 inline policy（使用上述輸出的 JSON）
4. 建立 Access Key（選擇「Application running outside AWS」）
5. 產生 SMTP 密碼：
   - 使用 AWS 官方 Python 腳本轉換 Secret Access Key
   - 或使用線上工具：https://awsses.com/smtp-credentials-generator

保存憑證：
- MAIL_USERNAME = Access Key ID
- MAIL_PASSWORD = 轉換後的 SMTP Password

### 4. 部署 DNS 記錄

前往 Cloudflare DNS-SES 模組部署 DNS 記錄：

```bash
cd ../../cloudflare/dns-ses
terraform init
terraform apply
```

DNS-SES 模組會自動讀取 SES 模組的輸出並建立所需記錄。

### 5. 驗證 DNS 記錄

等待 DNS 記錄生效（10-30 分鐘）：

```bash
# 驗證域名驗證記錄
nslookup -type=TXT _amazonses.example.com

# 驗證 MAIL FROM MX 記錄
nslookup -type=MX noreply.example.com

# 驗證 MAIL FROM SPF 記錄
nslookup -type=TXT noreply.example.com
```

### 6. 檢查 SES 驗證狀態

登入 AWS Console：
1. 前往 SES 服務頁面
2. 選擇 "Verified identities"
3. 確認域名狀態為 "Verified"

### 7. 申請脫離沙盒模式

新建立的 SES 帳號在沙盒模式有以下限制：
- 每日約 200 封配額
- 每秒 1 封速率
- 只能發送到已驗證的郵箱

**申請步驟：**

1. AWS Console → SES → Account dashboard
2. 點擊 "Request production access"
3. 填寫申請表單說明用途
4. 等待審核（1-2 個工作日）

### 8. 配置後端應用

編輯生產環境配置檔案：

```bash
# 郵件服務配置
MAIL_HOST=email-smtp.ap-south-1.amazonses.com
MAIL_PORT=587
MAIL_USERNAME=<步驟 3 的 Access Key ID>
MAIL_PASSWORD=<步驟 3 的 SMTP Password>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# 發件人資訊
MAIL_FROM_ADDRESS=noreply@example.com
MAIL_FROM_NAME=YourApp
```

### 9. 測試郵件發送

**沙盒模式：**
1. 在 AWS Console 驗證測試郵箱
2. 透過後端 API 發送測試郵件
3. 確認收件

**生產模式：**
- 申請通過後可發送到任何郵箱
- 檢查郵件標頭確認 DKIM/SPF 驗證通過

## 驗證

部署成功後：
- SES 域名狀態顯示 "Verified"
- DKIM 記錄驗證通過
- MAIL FROM 域名配置正確
- 後端應用可成功發送郵件

## 完成

AWS SES 已部署完成，可透過 SMTP 發送郵件。
