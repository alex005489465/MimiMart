# AWS Key Pair 模組

## 目標

管理 EC2 實例的 SSH 金鑰對，上傳公鑰至 AWS 供 EC2 使用。

## 前置條件

- AWS 認證已配置於 `aws/.env`
- 已使用 `modules/ssh-keygen` 生成 SSH 金鑰對
- Docker 正在運行

## 執行步驟

### 1. 生成 SSH 金鑰

```bash
cd ../../modules/ssh-keygen
cp config.json.example config.json
# 編輯 config.json，設定金鑰名稱
npm run generate
```

### 2. 複製配置範例

```bash
cd ../../aws/key-pair
cp terraform.tfvars.example terraform.tfvars
```

### 3. 編輯配置檔案

編輯 `terraform.tfvars`：

```hcl
project_name = "your-project-name"
environment  = "prod"
aws_region   = "ap-south-1"
key_name     = "your-project-env-app"
public_key   = "ssh-ed25519 AAAAAAAA... your-key-comment"
```

公鑰來源：
- 複製自 `modules/ssh-keygen/output/{name}.terraform.txt`
- 或複製自 `modules/ssh-keygen/output/{name}.pub`

### 4. 初始化 Terraform

```bash
cd ../..
docker-compose --env-file aws/.env run --rm terraform "cd key-pair && terraform init"
```

### 5. 檢視執行計畫

```bash
docker-compose --env-file aws/.env run --rm terraform "cd key-pair && terraform plan"
```

### 6. 部署 Key Pair

```bash
docker-compose --env-file aws/.env run --rm terraform "cd key-pair && terraform apply"
```

## 驗證

查看輸出資訊：

```bash
docker-compose --env-file aws/.env run --rm terraform "cd key-pair && terraform output"
```

檢查 AWS Console：
- 服務：EC2 → Network & Security → Key Pairs
- 確認 Key Pair 名稱和指紋

## 使用金鑰連線

```bash
ssh -i path/to/ssh-keygen/output/{name}.pem ec2-user@<ec2-ip>
```

## 完成

Key Pair 已部署至 AWS，可用於 EC2 實例配置。
