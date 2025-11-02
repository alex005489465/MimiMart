# AWS 基礎設施管理

模組化管理 AWS 基礎設施，每個子目錄代表獨立的基礎設施模組。

## 目錄結構

```
aws/
├── .env                    # AWS 認證資訊（所有模組共用）
├── .env.example           # 認證資訊範例
├── vpc/                   # VPC 網路模組
├── eip/                   # Elastic IP 模組
├── security-groups/       # 安全組模組
├── ec2/                   # EC2 計算實例模組
└── s3/                    # S3 物件儲存模組
```

## 前置設定

### 配置 AWS 認證資訊

```bash
# 複製並編輯配置檔
cp .env.example .env

# 填入 AWS Access Key 和 Secret Key
# TF_VAR_aws_access_key=[AWS_ACCESS_KEY]
# TF_VAR_aws_secret_key=[AWS_SECRET_KEY]
```

取得金鑰：登入 [AWS IAM Console](https://console.aws.amazon.com/iam/) → 建立 IAM 使用者 → 產生 Access Key

## 已部署模組

### VPC 模組 (`vpc/`)

基礎網路設施：VPC、公私有子網路、Internet Gateway、NAT Gateway、路由表

詳見：[vpc/README.md](vpc/README.md)

### EIP 模組 (`eip/`)

Elastic IP 靜態公有 IP 位址管理，可用於 EC2 實例、NAT Gateway 等資源

詳見：[eip/README.md](eip/README.md)

### Security Groups 模組 (`security-groups/`)

安全組（防火牆規則）管理：
- **Web**：HTTP/HTTPS 流量
- **Git**：Gitea SSH 流量（Web UI 透過 nginx）
- **SSH**：伺服器管理連線

詳見：[security-groups/README.md](security-groups/README.md)

### EC2 模組 (`ec2/`)

EC2 計算實例管理：虛擬伺服器、SSH 金鑰對、根磁碟配置、Elastic IP 關聯

詳見：[ec2/README.md](ec2/README.md)

### S3 模組 (`s3/`)

S3 物件儲存管理：
- **Static Bucket**：商品圖片等靜態資源，通過 Cloudflare CDN 公開存取
- **Log Bucket**：應用程式日誌，完全私有，自動生命週期管理
- **IAM 政策**：後端 S3 存取權限（手動建立 IAM User）

詳見：[s3/README.md](s3/README.md)

## 部署順序

1. EIP（無依賴）
2. VPC（無依賴，或依賴 EIP 如需用於 NAT Gateway）
3. Security Groups（依賴 VPC）
4. EC2（依賴 VPC、Security Groups、選擇性依賴 EIP）
5. S3（無依賴）

## 使用方式

> **注意**：所有指令需要在 `infra/cloud-manage` 目錄下執行

```bash
# 通用指令模板
docker-compose --env-file aws/.env run --rm terraform "cd <module> && terraform <command>"
```

## 配置原則

- **所有模組變數均不提供預設值**：確保明確性與可控性，避免意外配置
- **範例檔案僅使用通用佔位符**：`terraform.tfvars.example` 中使用測試用 IP 和佔位符，避免洩露實際配置

## 安全注意

- **絕不提交 `.env` 至版本控制**
- 定期輪換 AWS Access Key
- 使用 IAM 最小權限原則
- 啟用 CloudTrail 審計日誌

## 參考資源

- [AWS Provider 文檔](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS 架構最佳實踐](https://aws.amazon.com/architecture/well-architected/)
