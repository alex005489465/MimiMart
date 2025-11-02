# IP Ranges 共用模組

## 目標

統一管理 Cloudflare IP 範圍和開發者 IP 白名單，供所有 Terraform 模組引用。

## 模組說明

此模組提供兩類 IP 範圍：

### 1. Cloudflare IP 範圍
- 來源：https://www.cloudflare.com/ips
- 儲存位置：`cloudflare-ips.tf`（硬編碼）
- 內容：IPv4 和 IPv6 CIDR 範圍

### 2. 開發者/管理員 IP
- 來源：`developer-ips.json`（本地檔案）
- 安全性：不提交到 Git（已加入 `.gitignore`）
- 內容：開發者和辦公室 IP（分 IPv4 和 IPv6）

## 前置條件

需要建立 `developer-ips.json` 配置檔案。

## 執行步驟

### 1. 建立開發者 IP 配置檔案

複製範例檔案並編輯：

```bash
cd modules/ip-ranges
cp developer-ips.json.example developer-ips.json
```

### 2. 填入實際 IP 地址

編輯 `developer-ips.json`，替換佔位符為實際的 IP 地址：

需要的資訊：
- 開發者的 IPv4 地址（格式：`1.2.3.4/32`）
- 開發者的 IPv6 地址（格式：`2001:db8::1/128`，可選）
- 辦公室固定 IP（可選）
- 更新日期（`last_updated` 欄位）

查詢當前 IP 的方式：
```bash
# 查詢 IPv4
curl -4 ifconfig.me

# 查詢 IPv6
curl -6 ifconfig.me
```

### 3. 在 Terraform 模組中引用

```hcl
module "ip_ranges" {
  source = "../../modules/ip-ranges"
}

# 使用 Cloudflare + 開發者 IP
resource "aws_s3_bucket_policy" "example" {
  # ...
  Condition = {
    IpAddress = {
      "aws:SourceIp" = module.ip_ranges.cloudflare_and_developers_v4
    }
  }
}
```

## 可用的輸出

| 輸出名稱 | 說明 | 適用場景 |
|---------|------|----------|
| `cloudflare_ipv4` | Cloudflare IPv4 範圍 | S3 Bucket Policy, WAF 規則 |
| `cloudflare_ipv6` | Cloudflare IPv6 範圍 | IPv6 支援的服務 |
| `cloudflare_all` | 所有 Cloudflare IP | 需要同時支援 IPv4/IPv6 |
| `developer_ips_v4` | 開發者 IPv4 | IPv4 限制場景 |
| `developer_ips_v6` | 開發者 IPv6 | IPv6 限制場景 |
| `developer_ips` | 所有開發者 IP | 一般管理存取 |
| `office_ips_v4` | 辦公室 IPv4 | IPv4 限制場景 |
| `office_ips_v6` | 辦公室 IPv6 | IPv6 限制場景 |
| `office_ips` | 所有辦公室 IP | 企業網路存取 |
| `cloudflare_and_developers_v4` | CF IPv4 + 開發者 IPv4 | S3 Bucket Policy（推薦） |
| `cloudflare_and_developers_v6` | CF IPv6 + 開發者 IPv6 | IPv6 公開資源 |
| `cloudflare_and_developers` | CF + 開發者（全部） | 開發環境公開資源 |
| `admin_only_v4` | 僅管理員 IPv4 | IPv4 管理端點 |
| `admin_only_v6` | 僅管理員 IPv6 | IPv6 管理端點 |
| `admin_only` | 僅管理員（全部） | 管理端點 |
| `all_trusted_ips` | 所有信任的 IP | 完整白名單 |

## 維護流程

### 更新開發者 IP

當開發者 IP 變更時：

1. 編輯 `modules/ip-ranges/developer-ips.json`
2. 更新 `last_updated` 日期
3. 重新執行受影響模組的 `terraform apply`

### 更新 Cloudflare IP

每季度檢查官方 IP 是否有變更：

1. 檢查最新 IP：https://www.cloudflare.com/ips
2. 如有變更，編輯 `modules/ip-ranges/cloudflare-ips.tf`
3. 更新檔案頂部的更新日期
4. 重新執行受影響模組的 `terraform apply`

## 驗證

執行 `terraform init`、`terraform plan` 和 `terraform output` 檢查配置是否正確。

## 安全注意事項

1. `developer-ips.json` 不會被提交到 Git
2. 團隊成員需各自建立自己的 `developer-ips.json`
3. IP 必須使用 CIDR 格式（單一 IP 使用 `/32` 或 `/128`）
4. 更新 IP 後需重新 apply 所有使用此模組的資源

## 完成

模組已配置完成，可供其他 Terraform 模組引用。
