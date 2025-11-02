# Cloudflare WAF 模組

Cloudflare Web Application Firewall (WAF) 自訂規則管理模組，用於配置 IP 白名單、地理位置封鎖等安全規則。

## 用途

管理 Cloudflare WAF 自訂規則，包括：
- IP 白名單/黑名單
- 域名訪問控制
- 地理位置封鎖
- API 端點保護
- 自訂安全規則

## 功能特性

- 支援多種規則動作（block、allow、challenge、log）
- 支援複雜的表達式語法
- 可針對特定域名、路徑、IP 設定規則
- 支援地理位置過濾
- 支援規則啟用/停用控制
- 使用 Terraform 管理，可版本控制

## 前置設定

### 1. 配置 Cloudflare 認證資訊

WAF 模組使用上層目錄的 `.env` 檔案（與其他模組共用）。

如果尚未配置，請編輯 `cloudflare/.env`：

```bash
TF_VAR_cloudflare_api_token=[CLOUDFLARE_API_TOKEN]
TF_VAR_cloudflare_zone_id=[CLOUDFLARE_ZONE_ID]
```

**API Token 權限需求**：
- `Zone:Firewall Services:Edit`
- `Zone:WAF:Edit`

### 2. 建立配置檔案

```bash
# 複製範例配置並修改
cp terraform.tfvars.example terraform.tfvars
```

編輯 `terraform.tfvars`，配置你的 WAF 規則。


## 部署步驟

> **注意**：所有指令需要在 `infra/cloud-manage` 目錄下執行

```bash
# 通用指令模板
docker-compose --env-file cloudflare/.env run --rm terraform "cd waf && terraform <command>"
```

## 規則表達式語法

### 域名匹配

| 表達式 | 說明 | 範例 |
|--------|------|------|
| `http.host eq "example.com"` | 完全匹配 | 僅匹配 example.com |
| `http.host contains "example"` | 包含字串 | 匹配任何包含 example 的域名 |
| `http.host in {"a.com" "b.com"}` | 多個域名 | 匹配 a.com 或 b.com |

### IP 匹配

| 表達式 | 說明 | 範例 |
|--------|------|------|
| `ip.src eq 203.0.113.50` | 單一 IPv4 | 來源 IP 等於 203.0.113.50 |
| `ip.src ne 203.0.113.50` | 不等於（⚠️ 不建議用於白名單） | 來源 IP 不等於 203.0.113.50 |
| `ip.src in {203.0.113.50 203.0.113.51}` | 多個 IPv4 | 來源 IP 在列表中 |
| `not ip.src in {203.0.113.50}` | 不在列表中（✅ 建議用於白名單） | 來源 IP 不在列表中 |
| `ip.src in {203.0.113.0/24}` | IP 範圍 | CIDR 表示法 |
| `ip.src in {203.0.113.50 2001:db8::1}` | IPv4 + IPv6 | 同時支援兩種協議 |

**⚠️ IPv6 重要提醒**：
- 現代網路通常同時支援 IPv4 和 IPv6
- 瀏覽器可能優先使用 IPv6 連線
- 如果白名單只有 IPv4，使用 IPv6 訪問時會被封鎖
- **建議**：檢查你的 IPv6 位址並同時加入白名單

### 路徑匹配

| 表達式 | 說明 | 範例 |
|--------|------|------|
| `http.request.uri.path eq "/admin"` | 完全匹配 | 僅匹配 /admin |
| `http.request.uri.path contains "/api/"` | 包含字串 | 包含 /api/ 的路徑 |
| `http.request.uri.path matches "^/admin.*"` | 正則表達式 | 以 /admin 開頭 |

### 地理位置

| 表達式 | 說明 | 範例 |
|--------|------|------|
| `ip.geoip.country eq "TW"` | 單一國家 | 台灣 |
| `ip.geoip.country in {"TW" "US"}` | 多個國家 | 台灣或美國 |

### 組合條件

| 運算子 | 說明 | 範例 |
|--------|------|------|
| `and` | 且 | `(condition1 and condition2)` |
| `or` | 或 | `(condition1 or condition2)` |
| `not` | 非 | `not (condition1)` |

## 規則動作類型

| 動作 | 說明 | 使用場景 | Free 方案 |
|------|------|----------|----------|
| `block` | 直接封鎖請求 | IP 黑名單、惡意流量 | ✅ 支援 |
| `allow` | 允許請求通過 | IP 白名單、信任的來源 | ✅ 支援 |
| `challenge` | 顯示 CAPTCHA 驗證碼 | 可疑但不確定的流量 | ✅ 支援 |
| `js_challenge` | JavaScript 驗證 | 檢測瀏覽器環境 | ✅ 支援 |
| `managed_challenge` | 智能驗證 | Cloudflare 自動判斷 | ✅ 支援 |
| `log` | 僅記錄，不攔截 | 測試規則、監控流量 | ❌ 不支援 |

## 費用說明

### Free 方案限制

- **WAF Custom Rules**：最多 5 條規則
- **費用**：完全免費
- **限制**：
  - ❌ 不支援 `log` 動作（僅記錄不攔截）
  - ✅ 支援其他所有動作類型（block、allow、challenge 等）
  - ✅ 支援完整的表達式語法

### 升級方案

如需更多規則或進階功能，可升級至：
- **Pro 方案**：$20/月，包含更多 WAF 功能和 `log` 動作
- **Business 方案**：$200/月，包含 25 條自訂規則

## 輸出資訊

部署後可查看以下輸出：

```bash
ruleset_id       # WAF 規則集 ID
ruleset_name     # WAF 規則集名稱
rules_count      # 規則總數
rules_summary    # 規則摘要（動作、描述、狀態）
```


## 測試規則

**注意**：Free 方案不支援 `log` 動作，建議使用以下方法測試：

### 方法 1：先停用規則
```hcl
{
  action      = "block"
  description = "測試規則"
  enabled     = false  # 先停用
  expression  = "(your expression here)"
}
```

部署後確認語法無誤，再改為 `enabled = true`。

### 方法 2：檢查 Cloudflare Dashboard
在 **Security** > **Events** 查看封鎖事件，確認：
- 實際的來源 IP（包括 IPv4 和 IPv6）
- 觸發規則的原因
- 封鎖是否符合預期

## 安全注意

- ⚠️ **不要鎖住自己**：設定 IP 白名單前，請確認你的 IP 正確（包括 IPv4 和 IPv6）
- 🌐 **IPv6 很重要**：現代網路通常同時支援 IPv4 和 IPv6，請務必兩者都加入白名單
- 📝 **先停用測試**：新規則先設定 `enabled = false` 測試，確認無誤後再啟用
- 🔄 **定期更新 IP**：如果你的 IP 會變動，考慮使用 IP 範圍或動態 DNS
- 🛡️ **多層防護**：WAF 規則應配合 EC2 安全組一起使用
- 📊 **監控事件**：定期檢查 Cloudflare Security Events，確認規則正常運作
- 💡 **優先檢查 Dashboard**：被封鎖時，先查看 Security Events 了解實際的來源 IP


## 疑難排解

### 問題 1：設定 IP 白名單後仍被封鎖（最常見）

**症狀**：明明加了自己的 IP，但訪問網站時還是被封鎖

**原因**：你的網路同時支援 IPv4 和 IPv6，瀏覽器使用 IPv6 連線，但規則只允許 IPv4

**診斷步驟**：
1. 前往 Cloudflare Dashboard → **Security** → **Events**
2. 查看最近的封鎖事件
3. 檢查 `clientIP` 欄位，看是 IPv4 還是 IPv6

**解決方案**：
```bash
# 1. 檢查你的 IPv4
curl https://api.ipify.org
# 輸出例如：203.0.113.50

# 2. 檢查你的 IPv6
curl -6 https://api64.ipify.org
# 輸出例如：2001:db8::1234
```

然後更新規則，同時加入兩個 IP：
```hcl
expression = "(http.host eq \"admin.example.com\" and not ip.src in {203.0.113.50 2001:db8::1234})"
```

### 問題 2：無法訪問網站（其他原因）

**症狀**：設定規則後無法訪問網站

**解決方案**：
1. 檢查你的當前 IP：`curl https://api.ipify.org`
2. 確認 terraform.tfvars 中的 IP 是否正確
3. 臨時停用規則：設定 `enabled = false` 並重新部署

### 問題 3：規則未生效

**症狀**：設定規則後流量仍然通過

**解決方案**：
1. 確認規則 `enabled = true`
2. 檢查表達式語法是否正確
3. 在 Cloudflare Dashboard 查看規則狀態
4. 清除瀏覽器快取或使用無痕模式測試

### 問題 4：超過規則數量限制

**症狀**：部署時提示超過 5 條規則限制

**解決方案**：
1. 合併相似規則（使用 `http.host in {}` 保護多個域名）
2. 合併 IP 列表（使用 `ip.src in {IP1 IP2 IP3}`）
3. 考慮升級至 Pro 或 Business 方案

### 問題 5：部署時提示不支援 log 動作

**症狀**：錯誤訊息 "not entitled to use the log action"

**原因**：Free 方案不支援 `log` 動作

**解決方案**：
- 改用 `block`、`allow` 或 `challenge` 動作
- 或升級至 Pro 方案以上

## 參考資源

- [Cloudflare WAF 文檔](https://developers.cloudflare.com/waf/)
- [Custom Rules 文檔](https://developers.cloudflare.com/waf/custom-rules/)
- [規則表達式語法](https://developers.cloudflare.com/ruleset-engine/rules-language/)
- [Cloudflare Terraform Provider](https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs/resources/ruleset)
