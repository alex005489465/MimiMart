# MimiMart 雲端基礎設施管理

使用容器化 Terraform 管理 MimiMart 專案的多雲平台基礎設施。

## 🏗️ 架構概覽

```
infra/cloud-manage/
├── docker-compose.yml         # 共用容器配置
├── cloudflare/               # Cloudflare 平台模組
│   ├── dns/                  # DNS 記錄管理
│   └── waf/                  # Web 應用防火牆規則
└── aws/                      # AWS 平台模組
    └── s3/                   # S3 物件儲存
```

## ✨ 設計理念

- **雲平台分離**: 每個平台獨立資料夾,各自管理配置和機密
- **自動平台切換**: 透過 `.env` 中的 `PLATFORM` 變數自動識別平台
- **容器化執行**: 零本機安裝,所有操作在容器內執行
- **非互動式設計**: 支援 CI/CD 和自動化腳本執行

## 🚀 快速開始

### 前置條件

- Docker 和 Docker Compose 已安裝
- 已配置對應平台的 `.env` 檔案

### 平台配置

每個平台都有獨立的 `.env` 檔案:

```bash
# Cloudflare 配置
cloudflare/.env       # 包含 PLATFORM=cloudflare 和 Cloudflare 認證資訊

# AWS 配置
aws/.env             # 包含 PLATFORM=aws 和 AWS 認證資訊
```

**PLATFORM 變數自動機制**:
- 使用 `--env-file cloudflare/.env` 時,PLATFORM 自動設為 `cloudflare`
- 使用 `--env-file aws/.env` 時,PLATFORM 自動設為 `aws`
- 容器會自動切換到對應平台的工作目錄

## 📖 使用方式

### 基本指令格式

```bash
docker-compose run --rm --env-file <platform>/.env terraform -c "cd <module> && terraform <command>"
```

### Cloudflare DNS 模組

```bash
# 初始化
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform init"

# 檢查變更計畫
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform plan"

# 套用變更
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform apply"

# 自動批准(用於 CI/CD)
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform apply -auto-approve"

# 查看輸出
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform output"

# 銷毀資源
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform destroy"
```

### Cloudflare WAF 模組

```bash
# 初始化
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd waf && terraform init"

# 檢查變更計畫
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd waf && terraform plan"

# 套用變更
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd waf && terraform apply"

# 查看輸出
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd waf && terraform output"

# 銷毀資源
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd waf && terraform destroy"
```

### AWS S3 模組

```bash
# 初始化
docker-compose run --rm --env-file aws/.env terraform -c "cd s3 && terraform init"

# 檢查變更計畫
docker-compose run --rm --env-file aws/.env terraform -c "cd s3 && terraform plan"

# 套用變更
docker-compose run --rm --env-file aws/.env terraform -c "cd s3 && terraform apply"

# 查看輸出
docker-compose run --rm --env-file aws/.env terraform -c "cd s3 && terraform output"

# 查看特定輸出(原始格式)
docker-compose run --rm --env-file aws/.env terraform -c "cd s3 && terraform output -raw backend_s3_policy_json"

# 銷毀資源
docker-compose run --rm --env-file aws/.env terraform -c "cd s3 && terraform destroy"
```

## 🛠️ 常用 Terraform 指令

### 格式化和驗證

```bash
# 格式化程式碼
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform fmt"

# 驗證配置
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform validate"

# 檢查 state 狀態
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform show"

# 列出 state 資源
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform state list"
```

### 進階操作

```bash
# 更新 provider 版本
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform init -upgrade"

# 目標性套用(只更新特定資源)
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform apply -target=cloudflare_record.example"

# 匯入現有資源
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform import cloudflare_record.example <record_id>"

# 移除 state 中的資源(不刪除實際資源)
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform state rm cloudflare_record.example"
```

## 📚 模組文檔

詳細的模組配置和使用說明,請參閱各模組的 README:

### Cloudflare 平台
- [DNS 模組](./cloudflare/dns/README.md) - DNS 記錄管理
- [WAF 模組](./cloudflare/waf/README.md) - Web 應用防火牆規則
- [Cloudflare 總覽](./cloudflare/README.md) - Cloudflare 平台說明

### AWS 平台
- [S3 模組](./aws/s3/README.md) - S3 物件儲存管理
- [AWS 總覽](./aws/README.md) - AWS 平台說明

## 🔒 安全注意事項

- **絕不提交 `.env` 至版本控制** - 所有 `.env` 檔案已加入 `.gitignore`
- **定期輪換 API 金鑰** - Cloudflare API Token 和 AWS Access Key
- **使用最小權限原則** - API Token 和 IAM 政策只授予必要權限
- **啟用審計日誌** - Cloudflare Audit Logs 和 AWS CloudTrail
- **保護 Terraform State** - 考慮使用遠端 backend(S3 + DynamoDB)

## 💡 最佳實踐

### 部署流程

1. **先執行 plan 檢查變更**
   ```bash
   docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform plan"
   ```

2. **確認無誤後執行 apply**
   ```bash
   docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform apply"
   ```

3. **查看輸出並記錄重要資訊**
   ```bash
   docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform output"
   ```

### CI/CD 整合

使用 `-auto-approve` 參數實現自動化部署:

```bash
# 在 CI/CD pipeline 中使用
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform apply -auto-approve"
```

### 錯誤處理

如果遇到 lock 問題:

```bash
# 強制解鎖(謹慎使用)
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform force-unlock <lock_id>"
```

## 🔄 部署順序建議

### 初次部署

1. **Cloudflare DNS** - 設定域名解析
2. **Cloudflare WAF** - 配置安全規則
3. **AWS S3** - 建立儲存桶

### 日常維護

- DNS 記錄變更: 隨時可執行,無依賴
- WAF 規則調整: 隨時可執行,無依賴
- S3 配置更新: 注意 CORS 和權限變更影響

## 📊 狀態管理

目前使用本地 state 檔案(`terraform.tfstate`),位於各模組目錄下。

**未來改進**: 考慮使用遠端 state backend:
- 使用 S3 + DynamoDB 實現 state locking
- 支援團隊協作
- 提供 state 版本歷史

## 🆘 疑難排解

### 問題: 找不到 terraform 指令

**解決方案**: 確認 Docker 正在執行,並且 `hashicorp/terraform:latest` 映像已拉取

```bash
docker pull hashicorp/terraform:latest
```

### 問題: 權限錯誤

**解決方案**: 檢查 `.env` 檔案中的認證資訊是否正確

```bash
# Cloudflare
TF_VAR_cloudflare_api_token=<your_token>
TF_VAR_cloudflare_zone_id=<your_zone_id>

# AWS
TF_VAR_aws_access_key=<your_access_key>
TF_VAR_aws_secret_key=<your_secret_key>
```

### 問題: State 衝突

**解決方案**: 確保同一時間只有一個人在執行 terraform 操作

```bash
# 如果確定沒有其他人在操作,可以強制解鎖
docker-compose run --rm --env-file cloudflare/.env terraform -c "cd dns && terraform force-unlock <lock_id>"
```

## 📝 參考資源

- [Terraform 官方文檔](https://www.terraform.io/docs)
- [Cloudflare Terraform Provider](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs)
- [AWS Terraform Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Docker Compose 文檔](https://docs.docker.com/compose/)

## 🏷️ 版本資訊

- **Terraform**: latest (由 Docker 映像提供)
- **專案**: MimiMart 雲端基礎設施
- **環境**: Production
