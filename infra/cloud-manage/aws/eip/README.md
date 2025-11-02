# AWS EIP Terraform 模組

## 目標

建立 AWS Elastic IP 資源,提供固定的公開 IP 位址。

## 前置條件

此模組無依賴其他模組,可獨立部署。

## 執行步驟

### 1. 準備配置檔

複製範例配置檔並編輯:

```bash
cp terraform.tfvars.example terraform.tfvars
```

需要的資訊:
- `project_name`: 專案名稱
- `environment`: 環境 (dev/staging/prod)
- `eip_count`: 要建立的 EIP 數量

### 2. 初始化與部署

```bash
# 初始化
docker-compose --env-file aws/.env run --rm terraform "cd eip && terraform init"

# 預覽變更
docker-compose --env-file aws/.env run --rm terraform "cd eip && terraform plan"

# 部署
docker-compose --env-file aws/.env run --rm terraform "cd eip && terraform apply"
```

### 3. 查看輸出

```bash
# 查看所有 outputs
docker-compose --env-file aws/.env run --rm terraform "cd eip && terraform output"

# 查看 allocation IDs (供 EC2 關聯使用)
docker-compose --env-file aws/.env run --rm terraform "cd eip && terraform output eip_allocation_ids"
```

## 驗證

檢查建立的 EIP:

```bash
# 查看公開 IP
docker-compose --env-file aws/.env run --rm terraform "cd eip && terraform output eip_public_ips"

# 或使用 AWS CLI
aws ec2 describe-addresses --region <YOUR_REGION>
```

## 完成

Elastic IP 已建立,可供 EC2 實例關聯使用。
