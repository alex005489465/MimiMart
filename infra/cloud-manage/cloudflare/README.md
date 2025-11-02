# Cloudflare 基礎設施管理

模組化管理 Cloudflare 基礎設施，每個子目錄代表獨立的基礎設施模組。

## 目錄結構

```
cloudflare/
├── .env                    # Cloudflare 認證資訊（所有模組共用）
├── .env.example           # 認證資訊範例
├── dns/                   # DNS 記錄模組
└── waf/                   # Web 應用防火牆模組
```

## 前置設定

### 配置 Cloudflare 認證資訊

```bash
# 複製並編輯配置檔
cp .env.example .env

# 填入 Cloudflare API Token、Zone ID 和 Account ID
```

取得資訊：
- **API Token**: 前往 https://dash.cloudflare.com/profile/api-tokens
  - DNS 模組需要「編輯 Zone DNS」權限
  - WAF 模組需要「Zone:Firewall Services:Edit」和「Zone:WAF:Edit」權限
- **Zone ID 與 Account ID**: 登入 Cloudflare Dashboard → 選擇網域 → 右側欄位顯示

## 已部署模組

### DNS 模組 (`dns/`)

獨立 DNS 記錄管理，例如應用服務子網域、郵件伺服器、API 子網域等

詳見：[dns/README.md](dns/README.md)

### WAF 模組 (`waf/`)

Cloudflare Web Application Firewall 自訂規則管理，用於配置 IP 白名單、地理位置封鎖等安全規則

詳見：[waf/README.md](waf/README.md)

## 部署順序

1. **DNS**（無依賴）- 設定域名解析
2. **WAF**（無依賴）- 配置安全規則

## 使用方式

> **注意**：所有指令需要在專案根目錄 `infra/cloud-manage` 下執行

```bash
# 通用指令模板
docker-compose --env-file cloudflare/.env run --rm terraform "cd <module> && terraform <command>"
```

## 配置原則

- **所有模組變數均不提供預設值**：確保明確性與可控性，避免意外配置
- **範例檔案僅使用通用佔位符**：`terraform.tfvars.example` 中使用佔位符，避免洩露實際配置

## 安全注意

- **絕不提交 `.env` 至版本控制**
- 定期輪換 Cloudflare API Token
- 使用 API Token 最小權限原則（僅授予必要的權限）
- 啟用 Cloudflare Audit Logs 監控變更

## 參考資源

- [Cloudflare Provider 文檔](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs)
- [Cloudflare DNS 文檔](https://developers.cloudflare.com/dns/)
- [Cloudflare WAF 文檔](https://developers.cloudflare.com/waf/)
