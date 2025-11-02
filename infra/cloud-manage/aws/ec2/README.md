# AWS EC2 Terraform 模組

## 目標

建立 AWS EC2 實例,並自動安裝 Docker 與 Docker Compose。

## 前置條件

部署此模組前需要完成:

1. VPC 模組 - 提供子網路 ID
2. Security Groups 模組 - 提供安全組 ID
3. Key Pair 模組 - 提供 SSH 金鑰名稱

## 執行步驟

### 1. 準備配置檔

複製範例配置檔並編輯:

```bash
cp terraform.tfvars.example terraform.tfvars
```

需要的資訊:
- `project_name`: 專案名稱
- `environment`: 環境 (dev/staging/prod)
- `ami_id`: Amazon Linux 2023 AMI ID
- `instance_type`: 實例類型
- `subnet_ids`: VPC 模組的子網路 ID
- `security_group_ids`: Security Groups 模組的安全組 ID
- `key_name`: Key Pair 模組的金鑰名稱

### 2. 查詢相依模組的 Outputs

取得其他模組建立的資源 ID:

```bash
# VPC 模組
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform output -json"

# Security Groups 模組
docker-compose --env-file aws/.env run --rm terraform "cd security-groups && terraform output -json"

# Key Pair 模組
docker-compose --env-file aws/.env run --rm terraform "cd key-pair && terraform output -json"
```

### 3. 查詢 AMI ID

使用 AWS CLI 查詢最新的 Amazon Linux 2023 AMI:

```bash
# x86_64 架構
aws ec2 describe-images \
  --region <YOUR_REGION> \
  --owners amazon \
  --filters "Name=name,Values=al2023-ami-2023.*-x86_64" \
            "Name=state,Values=available" \
  --query "Images | sort_by(@, &CreationDate) | [-1].[ImageId,Name]" \
  --output table
```

或透過 Docker Compose:

```bash
docker-compose --env-file aws/.env run --rm aws-cli \
  ec2 describe-images \
  --owners amazon \
  --filters "Name=name,Values=al2023-ami-2023.*-x86_64" "Name=state,Values=available" \
  --query "Images | sort_by(@, &CreationDate) | [-1].[ImageId,Name]" \
  --output table
```

### 4. 初始化與部署

```bash
# 初始化
docker-compose --env-file aws/.env run --rm terraform "cd ec2 && terraform init"

# 預覽變更
docker-compose --env-file aws/.env run --rm terraform "cd ec2 && terraform plan"

# 部署
docker-compose --env-file aws/.env run --rm terraform "cd ec2 && terraform apply"
```

### 5. 查看連線資訊

```bash
docker-compose --env-file aws/.env run --rm terraform "cd ec2 && terraform output"
```

## 驗證

連線到 EC2 實例並驗證 Docker 安裝:

```bash
# SSH 連線 (使用 output 中的 IP)
ssh -i <private-key-path> ec2-user@<INSTANCE_IP>

# 驗證 Docker
docker --version
docker-compose --version
```

User Data 腳本執行日誌:

```bash
sudo cat /var/log/cloud-init-output.log
```

## 完成

EC2 實例已建立並安裝 Docker,可開始部署應用程式。
