# Cloudflare Pages 模組

管理 Cloudflare Pages 專案，用於部署前端單頁應用程式（SPA）。

## 功能特性

- ✅ 建立和管理 Pages 專案
- ✅ 配置自訂域名
- ✅ 設定生產和預覽環境
- ✅ Direct Upload 部署方式（本地構建）
- ✅ 環境變數管理

## 前置需求

### 1. Cloudflare API Token

前往 [Cloudflare API Tokens](https://dash.cloudflare.com/profile/api-tokens) 建立 Token，需要以下權限：

- **Account - Cloudflare Pages: Edit**
- **Zone - DNS: Edit**（如需配置自訂域名）

### 2. 取得 Account ID

登入 Cloudflare Dashboard → 選擇任一網域 → 右側欄位可見 Account ID

### 3. 配置環境變數

編輯 `cloudflare/.env`：

```bash
# Cloudflare 認證
TF_VAR_cloudflare_api_token=your_api_token_here
TF_VAR_cloudflare_account_id=your_account_id_here
TF_VAR_domain_name=example.com
```

## 配置說明

### 複製並編輯配置檔

```bash
cd infra/cloud-manage/cloudflare/pages
cp terraform.tfvars.example terraform.tfvars
```

編輯 `terraform.tfvars`：

```hcl
# Pages 專案名稱
pages_project_name = "app1"

# 生產分支
production_branch = "main"

# 自訂域名
custom_domain = "app1.example.com"

# 環境變數（可選）
production_environment_variables = {}
preview_environment_variables = {}
```

## 使用方式

> **重要**：所有指令需要在專案根目錄 `infra/cloud-manage` 下執行

### 初始化 Terraform

```bash
docker-compose --env-file cloudflare/.env run --rm terraform \
  "cd pages && terraform init"
```

### 檢查變更計畫

```bash
docker-compose --env-file cloudflare/.env run --rm terraform \
  "cd pages && terraform plan"
```

### 套用配置

```bash
docker-compose --env-file cloudflare/.env run --rm terraform \
  "cd pages && terraform apply"
```

### 查看輸出資訊

```bash
docker-compose --env-file cloudflare/.env run --rm terraform \
  "cd pages && terraform output"
```

輸出範例：

```
pages_url           = "https://app1.pages.dev"
custom_domain_url   = "https://app1.example.com"
pages_project_name  = "app1"
```

## 部署流程

### 步驟一：建立 Pages 專案（首次）

```bash
cd infra/cloud-manage

# 初始化並套用 Terraform 配置
docker-compose --env-file cloudflare/.env run --rm terraform \
  "cd pages && terraform init && terraform apply"
```

### 步驟二：構建前端應用

```bash
cd src/frontend-app

# 使用容器構建
./scripts/build-in-container.sh
```

### 步驟三：部署到 Pages

使用 Wrangler CLI 上傳構建產物：

```bash
cd src/frontend-app

# 首次需要登入
npx wrangler login

# 部署到 Pages
npx wrangler pages deploy dist --project-name=app1
```

### 步驟四：驗證部署

訪問以下 URL 確認部署成功：

- **Pages 自動域名**: https://app1.pages.dev
- **自訂域名**: https://app1.example.com

## 資源說明

### cloudflare_pages_project

管理 Pages 專案，包含：

- 專案名稱和生產分支
- 構建配置（本專案使用本地構建）
- 環境變數設定
- 相容性設定

### cloudflare_pages_domain

為 Pages 專案配置自訂域名，自動處理：

- DNS 記錄配置
- SSL 憑證設定
- 域名驗證

## 環境變數配置

如果您的前端應用需要環境變數，有兩種方式：

### 方式一：構建時注入（推薦）

在本地構建時透過 `.env.production` 注入：

```bash
# .env.production
VITE_API_BASE_URL=https://api.example.com
VITE_APP_ENV=production
```

### 方式二：Pages 環境變數

在 `terraform.tfvars` 配置（較少使用）：

```hcl
production_environment_variables = {
  VITE_API_BASE_URL = "https://api.example.com"
}
```

## 常見問題

### Q: 部署後訪問自訂域名出現 DNS 錯誤？

A: 確保 DNS 模組已正確配置 CNAME 記錄指向 Pages 域名。

### Q: 如何回滾到先前版本？

A: 在 Cloudflare Dashboard 的 Pages 專案頁面可以查看部署歷史並回滾。

### Q: 部署失敗怎麼辦？

A: 檢查以下項目：
1. Wrangler 是否已登入：`npx wrangler whoami`
2. 專案名稱是否正確
3. dist 目錄是否存在且包含構建產物

### Q: 如何更新環境變數？

A: 修改 `terraform.tfvars` 後執行 `terraform apply`，或直接在 Cloudflare Dashboard 修改。

## 清理資源

```bash
docker-compose --env-file cloudflare/.env run --rm terraform \
  "cd pages && terraform destroy"
```

**警告**：這將刪除 Pages 專案及所有部署歷史。

## 後續步驟

1. 配置 DNS 記錄（參考 `dns` 模組）
2. 設定 WAF 規則保護應用（參考 `waf` 模組）
3. 整合 CI/CD 自動部署

## 參考資源

- [Cloudflare Pages 文檔](https://developers.cloudflare.com/pages/)
- [Wrangler CLI 文檔](https://developers.cloudflare.com/workers/wrangler/)
- [Terraform Cloudflare Provider](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs/resources/pages_project)
