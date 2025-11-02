# Cloudflare DNS 模組

獨立 DNS 記錄管理模組，用於配置與 Cloudflare Pages 無關的 DNS 記錄。

## 用途

管理各類 DNS 記錄，例如：
- 應用服務子網域（API、管理介面等）
- 郵件伺服器（MX、TXT）
- 網域驗證記錄（TXT）
- 其他服務的 A/AAAA/CNAME 記錄

## 功能特性

- 支援多種 DNS 記錄類型（A、AAAA、CNAME、MX、TXT、SRV、CAA、NS、PTR）
- 支援 Cloudflare 代理（proxied）選項
- 支援自訂 TTL 值
- 支援 MX 和 SRV 記錄的優先級設定
- 支援記錄備註說明
- 使用 `for_each` 實現靈活的多記錄管理

## 前置設定

### 1. 配置 Cloudflare 認證資訊

DNS 模組使用上層目錄的 `.env` 檔案（與 origin-ca、pages 模組共用）。

如果尚未配置，請編輯 `cloudflare/.env`：

```bash
TF_VAR_cloudflare_api_token=[CLOUDFLARE_API_TOKEN]
TF_VAR_cloudflare_zone_id=[CLOUDFLARE_ZONE_ID]
```

**API Token 權限需求**：`Zone:DNS:Edit`

### 2. 建立配置檔案

```bash
# 複製範例配置並修改
cp terraform.tfvars.example terraform.tfvars
```

編輯 `terraform.tfvars`，配置你的 DNS 記錄。

## 使用範例

### 範例 1：基本 A 記錄（指向 EC2）

```hcl
dns_records = [
  {
    name    = "subdomain.project"  # 完整域名：subdomain.project.example.com
    type    = "A"
    content = "[IP_ADDRESS]"
    proxied = true
    ttl     = 1
    comment = "Git UI 管理介面"
  }
]
```

**name 格式說明**：
- 基礎域名在 `cloudflare/.env` 中定義（例如 `example.com`）
- `name` 是基礎域名之前的部分
- 例如：`name = "subdomain.project"` → 完整域名 `subdomain.project.example.com`
- 特殊值：`name = "@"` 代表根域名本身

### 範例 2：多條記錄

```hcl
dns_records = [
  # API 服務
  {
    name    = "api"
    type    = "A"
    content = "[IP_ADDRESS_1]"
    proxied = true
    ttl     = 1
    comment = "API 伺服器"
  },

  # CNAME 指向外部服務
  {
    name    = "blog"
    type    = "CNAME"
    content = "yourblog.medium.com"
    proxied = false
    ttl     = 3600
    comment = "部落格服務"
  },

  # 郵件伺服器
  {
    name     = "@"
    type     = "MX"
    content  = "mail.example.com"
    proxied  = false
    ttl      = 3600
    priority = 10
    comment  = "主要郵件伺服器"
  }
]
```

### 範例 3：網域驗證 TXT 記錄

```hcl
dns_records = [
  {
    name    = "@"
    type    = "TXT"
    content = "v=spf1 include:_spf.google.com ~all"
    proxied = false
    ttl     = 3600
    comment = "SPF 記錄"
  }
]
```

## 部署步驟

> **注意**：所有指令需要在 `infra/cloud-manage` 目錄下執行

```bash
# 1. 初始化 Terraform
docker-compose run --rm --env-file cloudflare/.env terraform "cd dns && terraform init"

# 2. 檢查變更計畫
docker-compose run --rm --env-file cloudflare/.env terraform "cd dns && terraform plan"

# 3. 套用變更
docker-compose run --rm --env-file cloudflare/.env terraform "cd dns && terraform apply"

# 4. 查看輸出
docker-compose run --rm --env-file cloudflare/.env terraform "cd dns && terraform output"
```

## 配置參數說明

### 必填參數

| 參數 | 說明 | 範例 |
|------|------|------|
| `name` | DNS 記錄名稱（基礎域名之前的部分） | `subdomain.project`、`api`、`@`（根網域） |
| `type` | 記錄類型 | `A`、`AAAA`、`CNAME`、`MX`、`TXT` |
| `content` | 記錄內容 | IP 地址、域名或文字 |

### 選填參數

| 參數 | 預設值 | 說明 |
|------|--------|------|
| `proxied` | `false` | 是否啟用 Cloudflare 代理（橙色雲朵） |
| `ttl` | `1` | TTL 值（秒），`1` 表示自動 |
| `priority` | `null` | 優先級（僅 MX 和 SRV 需要） |
| `comment` | `null` | 記錄備註說明 |

## Cloudflare 代理說明

### 何時啟用代理（`proxied = true`）

- **網站流量**：需要 CDN、DDoS 防護、WAF
- **隱藏源 IP**：不想暴露真實伺服器 IP
- **需要 Cloudflare 功能**：Page Rules、Workers、Firewall Rules

**注意**：啟用代理時，DNS 查詢會返回 Cloudflare 的 IP，而非你的源 IP。

### 何時不啟用代理（`proxied = false`）

- **郵件記錄**：MX、SPF、DKIM 等
- **直連服務**：需要直接訪問源伺服器
- **非 HTTP/HTTPS 流量**：SSH、FTP、遊戲伺服器等
- **第三方服務驗證**：某些服務要求 DNS 記錄必須是 DNS-only

## 輸出資訊

部署後可查看以下輸出：

```bash
dns_records          # 所有 DNS 記錄的完整資訊
dns_record_ids       # 記錄 ID 對照表
dns_record_hostnames # 完整主機名稱列表
```

## 常見場景

### 場景 1：將子網域指向 EC2

```hcl
{
  name    = "admin"
  type    = "A"
  content = "[IP_ADDRESS_2]"
  proxied = true
  ttl     = 1
}
```

### 場景 2：將子網域指向 Vercel

```hcl
{
  name    = "app"
  type    = "CNAME"
  content = "cname.vercel-dns.com"
  proxied = false  # Vercel 建議不啟用代理
  ttl     = 3600
}
```

### 場景 3：配置 Google Workspace 郵件

```hcl
dns_records = [
  {
    name     = "@"
    type     = "MX"
    content  = "aspmx.l.google.com"
    proxied  = false
    ttl      = 3600
    priority = 1
  },
  {
    name    = "@"
    type    = "TXT"
    content = "v=spf1 include:_spf.google.com ~all"
    proxied = false
    ttl     = 3600
  }
]
```

## 安全注意

- 不要提交 `terraform.tfvars` 至版本控制
- 使用 API Token 最小權限原則（僅授予 DNS Edit 權限）
- 定期審查 DNS 記錄，移除不再使用的記錄
- 敏感服務（如管理介面）考慮使用 Cloudflare Access 進行存取控制

## 參考資源

- [Cloudflare DNS Records 文檔](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs/resources/record)
- [DNS 記錄類型說明](https://developers.cloudflare.com/dns/manage-dns-records/reference/dns-record-types/)
- [Cloudflare 代理狀態說明](https://developers.cloudflare.com/dns/manage-dns-records/reference/proxied-dns-records/)
