# MimiMart 網關服務

## 目標

提供基於 Cloudflare Tunnel 和 Nginx 的安全網關服務,處理外部回調請求(如金流回調)。

## 前置條件

- Docker 和 Docker Compose
- Node.js >= 14.0.0
- Cloudflare 帳號並建立 Tunnel
- 後端服務已在 `mimimart-network` Docker 網路中執行

## 執行步驟

### 1. 建立 Cloudflare Tunnel

前往 [Cloudflare Zero Trust Dashboard](https://one.dash.cloudflare.com/)：

1. 進入 **Access** > **Tunnels**
2. 建立新 Tunnel,記錄 Token 和 Tunnel ID
3. 在 **Public Hostname** 設定子網域指向 `http://nginx:80`

### 2. 生成憑證

```bash
cd cf-credentials-init
cp .env.token.example .env.token
# 編輯 .env.token 填入你的 Token
node init-credentials.js
```

### 3. 複製配置範本

```bash
cd ..
cp nginx/nginx.conf.example nginx/nginx.conf
cp nginx/conf.d/domain-example-payment.conf.example nginx/conf.d/payment.conf
cp cloudflared/config/config.yml.example cloudflared/config/config.yml

# 編輯 payment.conf 替換佔位符
# - your-payment-domain.example.com → 實際域名
# - your_backend_container → mimimart-java
# - your_backend_port → 8080
```

### 4. 啟動服務

```bash
docker-compose up -d
```

## 驗證

測試健康檢查端點：

```bash
# 內部測試
docker exec mimimart-gateway-nginx wget -qO- http://localhost/health

# 外部測試(使用你設定的域名)
curl https://<your-domain>/health
```

預期回應：HTTP 200 with "OK"

## 多域名配置

本網關支援透過單一 Tunnel 服務多個域名，每個域名使用獨立的 Nginx 配置文件。

### 配置流程

#### 1. 為每個域名建立 Nginx 配置

```bash
cd nginx/conf.d

# 選擇合適的範例文件複製
cp domain-example-payment.conf.example payment.conf    # 金流回調
cp domain-example-api.conf.example api.conf            # API 服務
cp domain-example-webhook.conf.example webhook.conf    # Webhook 接收

# 編輯配置文件，替換佔位符：
# - your-<purpose>-domain.example.com → 實際域名
# - your_backend_container → mimimart-java
# - your_backend_port → 8080
```

#### 2. 設定 DNS 記錄（使用 Terraform）

```bash
# 取得 Tunnel ID
cat cloudflared/config/credentials.json | jq -r '.TunnelID'
# 輸出範例: d7fdbe06-90a8-42aa-9795-079b4122e3c9

# 編輯 Terraform 配置
cd ../../../cloud-manage/cloudflare/dns
nano terraform.tfvars
```

在 `terraform.tfvars` 中添加 DNS 記錄：

```hcl
dns_records = [
  {
    name    = "payment"
    type    = "CNAME"
    content = "d7fdbe06-90a8-42aa-9795-079b4122e3c9.cfargotunnel.com"
    proxied = true
    comment = "Payment callback domain"
  },
  {
    name    = "api"
    type    = "CNAME"
    content = "d7fdbe06-90a8-42aa-9795-079b4122e3c9.cfargotunnel.com"
    proxied = true
    comment = "API domain"
  }
]
```

執行 Terraform：

```bash
terraform plan
terraform apply
```

#### 3. 重新載入 Nginx

```bash
cd ../../../dev-env/gateway
docker-compose exec nginx nginx -s reload
```

### 詳細說明

完整的多域名配置指引請參考 `nginx/conf.d/README.md`。

## 管理指令

啟動服務：
```bash
docker-compose up -d
```

停止服務：
```bash
docker-compose down
```

查看日誌：
```bash
docker-compose logs -f
```

重新載入 Nginx 配置：
```bash
docker-compose exec nginx nginx -s reload
```

## 完成

網關服務已設定完成,可接收外部請求並轉發至後端服務。
