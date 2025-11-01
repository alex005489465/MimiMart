# Cloudflare Tunnel Credentials 初始化工具

這是一個獨立的 Node.js 工具，用於將 Cloudflare Tunnel Token 轉換為 `credentials.json` 檔案。

## 用途

將 `CLOUDFLARE_TUNNEL_TOKEN` 轉換為 Cloudflare Tunnel 所需的 `credentials.json` 格式，並輸出到 `../cloudflared/config/credentials.json`。

## 為什麼需要這個工具？

- **避免修改官方容器**: 不需要自訂 cloudflared Docker 映像
- **簡化配置**: 只需提供 Token，自動生成憑證檔案
- **本機執行**: 無需額外的 Docker 容器
- **單一職責**: 專注於憑證轉換，不干擾其他服務
- **獨立管理**: Token 存放在工具目錄內，與容器配置分離

## Token 格式

Cloudflare Tunnel Token 包含三個欄位：

```json
{
  "a": "account_id",
  "t": "tunnel_id",
  "s": "secret"
}
```

Token 可能是：
1. Base64 編碼的 JSON（通常從 Cloudflare Dashboard 複製的格式）
2. 純 JSON 字串

## 使用方式

### 推薦方法：使用 .env.token 檔案

```bash
# 1. 複製範本文件
cp .env.token.example .env.token

# 2. 編輯 .env.token，填入你的 Token
nano .env.token
# 或使用其他編輯器

# 3. 執行初始化腳本
node init-credentials.js
# 或使用 npm
npm run init
```

### 替代方法：使用環境變數

```bash
# 設定環境變數
export CLOUDFLARE_TUNNEL_TOKEN="your_token_here"

# 執行腳本
node init-credentials.js
```

## 輸出

成功執行後，會在 `../cloudflared/config/credentials.json` 生成憑證檔案：

```json
{
  "AccountTag": "account_id",
  "TunnelID": "tunnel_id",
  "TunnelSecret": "secret"
}
```

## 冪等性

- 如果 `credentials.json` 已存在，腳本會跳過生成
- 若需重新生成，請先刪除現有檔案：
  ```bash
  npm run clean
  # 或
  rm ../cloudflared/config/credentials.json
  ```

## 錯誤處理

腳本會檢查並提示以下錯誤：

1. **未設定環境變數**: 提示如何設定 `CLOUDFLARE_TUNNEL_TOKEN`
2. **Token 格式錯誤**: 無法解析為 JSON
3. **缺少必要欄位**: Token 中缺少 `a`、`t` 或 `s` 欄位

## 安全注意事項

- `.env.token` 和 `credentials.json` 包含敏感資訊，已加入 `.gitignore`
- 請勿將 Token 或憑證檔案提交到版本控制
- `.env.token` 僅在初始化時需要，完成後可以刪除
- 容器運行時使用生成的 `credentials.json`，不需要 Token

## 取得 Token

1. 登入 [Cloudflare Zero Trust Dashboard](https://one.dash.cloudflare.com/)
2. 進入 **Access** > **Tunnels**
3. 建立新的 Tunnel 或選擇現有 Tunnel
4. 複製 Token（通常在安裝步驟中顯示）

## 下一步

生成憑證後：

1. 確認 `../cloudflared/config/config.yml` 中的 `tunnel` ID 與憑證一致
2. 啟動網關服務：
   ```bash
   cd ..
   docker-compose up -d
   ```

## 依賴

無需額外安裝依賴，僅使用 Node.js 內建模組：
- `fs`: 檔案系統操作
- `path`: 路徑處理

## 系統需求

- Node.js >= 14.0.0
