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
cp nginx/conf.d/backend.conf.example nginx/conf.d/backend.conf
cp cloudflared/config/config.yml.example cloudflared/config/config.yml
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
