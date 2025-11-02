# AWS Security Groups 模組

## 目標

管理 EC2 實例的安全組規則,控制入站和出站流量。

## 模組說明

**啟用的安全組:**
- SSH Security Group: 允許管理員 SSH 連線 (Port 22)

**保留的安全組:**
- Web Security Group: 允許 Cloudflare HTTP/HTTPS 流量 (目前全部註解)

**IP 範圍管理:**
- 管理員 IP 和 Cloudflare IP 由 `modules/ip-ranges` 模組統一管理
- 無需在此模組手動配置 IP 清單

## 建立的資源

| 資源 | 說明 |
|------|------|
| SSH Security Group | 僅允許管理員 IP SSH 連線 |
| SSH 入站規則 (IPv4/IPv6) | Port 22 從管理員 IP |
| SSH 出站規則 | 允許所有出站流量 |

## 前置條件

1. VPC 模組已部署
2. 管理員 IP 已配置於 `modules/ip-ranges/developer-ips.json`

## 執行步驟

### 1. 取得 VPC ID

```bash
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform output vpc_id"
```

記錄輸出的 VPC ID。

### 2. 配置變數

```bash
cp terraform.tfvars.example terraform.tfvars
```

編輯 `terraform.tfvars`,填入必要變數:

```hcl
project_name = "your-project"
environment  = "prod"
vpc_id       = "vpc-xxxxxxxxx"  # 步驟 1 取得的 VPC ID
aws_region   = "ap-south-1"
```

### 3. 部署

```bash
# 初始化
docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform init"

# 檢查計畫
docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform plan"

# 部署
docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform apply"
```

## 驗證

查看建立的 Security Group ID:

```bash
docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform output ssh_sg_id"
```

## 輸出變數

| 變數 | 說明 |
|------|------|
| `ssh_sg_id` | SSH Security Group ID |
| `all_sg_ids` | 所有 Security Group IDs (供 EC2 模組使用) |

## 如何啟用 Web Security Group

1. 編輯 `web-sg.tf`,移除所有註解符號 `#`
2. 編輯 `outputs.tf`,取消註解 Web SG 相關輸出
3. 執行 `terraform apply`

## 注意事項

- AWS Security Group 的 `description` 欄位僅支援 ASCII 字元
- 管理員 IP 更新後需重新執行 `terraform apply`
- SSH SG 預設僅允許 `modules/ip-ranges` 中定義的管理員 IP
