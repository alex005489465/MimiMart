# AWS VPC 模組

## 目標

建立 AWS VPC 基礎網路架構，包含 Internet Gateway、Public Subnet 和 Route Table。

## 架構規格

- VPC CIDR: 10.0.0.0/16
- 1 個 Public Subnet: 10.0.1.0/24
- Internet Gateway
- 無 Private Subnets、無 NAT Gateway

## 前置條件

1. Docker 與 Docker Compose 已安裝
2. AWS 帳號與 Access Key
3. 已設定 `../aws/.env` 檔案，包含：
   - `TF_VAR_aws_access_key`
   - `TF_VAR_aws_secret_key`

## 執行步驟

### 1. 配置變數

複製範例檔案並根據需求修改：

```bash
cp terraform.tfvars.example terraform.tfvars
```

編輯 `terraform.tfvars` 設定：
- `aws_region`: AWS 區域
- `project_name`: 專案名稱
- `environment`: 環境標籤（dev/staging/prod）
- `vpc_cidr`: VPC CIDR 區塊
- `public_subnet_cidr`: Public Subnet CIDR
- `availability_zone`: 可用區

### 2. 初始化 Terraform

> **注意**：所有指令需要在 `infra/cloud-manage` 目錄下執行

```bash
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform init"
```

### 3. 檢視執行計畫

```bash
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform plan"
```

### 4. 部署基礎設施

```bash
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform apply"
```

## 驗證

部署完成後，查看輸出值：

```bash
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform output"
```

應該會顯示：
- `vpc_id`
- `public_subnet_id`
- `internet_gateway_id`
- 等資源 ID

## 輸出值用途

其他模組（Security Groups、EC2）需要使用以下輸出值：

- `vpc_id`: VPC ID
- `public_subnet_id`: Subnet ID
- `public_route_table_id`: Route Table ID

取得特定輸出值：

```bash
docker-compose --env-file aws/.env run --rm terraform "cd vpc && terraform output -raw vpc_id"
```

## 完成

VPC 網路架構已建立完成，可以繼續部署 Security Groups 和 EC2 實例。
