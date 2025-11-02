# Nginx 域名配置

## 目標

為單一 Cloudflare Tunnel 配置多個域名，每個域名使用獨立的 Nginx 配置文件。

## 前置條件

- Cloudflare Tunnel 已建立並取得 Tunnel ID
- 後端服務已在 `mimimart-network` Docker 網路中執行
- 具備 Terraform 執行權限

## 執行步驟

### 1. 選擇並複製範例文件

根據用途選擇適合的範例文件：

| 範例文件 | 用途 | 特點 |
|---------|------|------|
| `domain-example-payment.conf.example` | 金流回調 | 限制特定路徑 |
| `domain-example-api.conf.example` | API 服務 | 轉發 `/api/` 路徑 |
| `domain-example-webhook.conf.example` | Webhook 接收 | 多服務路由 |

```bash
# 範例：建立金流回調配置
cp domain-example-payment.conf.example payment.conf
```

### 2. 編輯配置文件

替換以下佔位符：

| 佔位符 | 說明 | 範例值 |
|--------|------|--------|
| `your-<purpose>-domain.example.com` | 域名 | `payment.yourdomain.com` |
| `your_backend_container` | 後端容器名稱 | `mimimart-java` |
| `your_backend_port` | 後端端口 | `8080` |

### 3. 取得 Tunnel ID

```bash
cat ../../cloudflared/config/credentials.json | jq -r '.TunnelID'
```

輸出範例: `d7fdbe06-90a8-42aa-9795-079b4122e3c9`

### 4. 設定 DNS 記錄

編輯 Terraform 配置 `infra/cloud-manage/cloudflare/dns/terraform.tfvars`：

```hcl
dns_records = [
  {
    name    = "payment"
    type    = "CNAME"
    content = "<TUNNEL_ID>.cfargotunnel.com"
    proxied = true
    comment = "Payment callback domain"
  }
]
```

執行 Terraform：

```bash
cd <project-root>/infra/cloud-manage/cloudflare/dns
terraform plan
terraform apply
```

### 5. 重新載入 Nginx

```bash
cd <project-root>/infra/dev-env/gateway
docker-compose exec nginx nginx -s reload
```

## 驗證

測試配置語法：
```bash
docker-compose exec nginx nginx -t
```

測試端點：
```bash
# 內部測試
docker exec mimimart-gateway-nginx wget -qO- http://localhost/health

# 外部測試(使用實際域名)
curl https://<your-domain>/health
```

預期回應：HTTP 200

## 完成

域名配置已完成,可透過指定域名訪問對應的後端服務。
