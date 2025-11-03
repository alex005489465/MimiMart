# MimiMart CI/CD 系統

容器化的前端構建與部署系統。

## 架構設計

- **完全隔離**：所有構建與部署在容器內執行，本機僅需 Docker
- **源碼保護**：源碼目錄唯讀掛載
- **產物集中**：所有構建產物統一存放在 `dist/` 目錄
- **API 驅動**：透過 HTTP API 觸發構建與部署
- **Web 管理**：提供簡單的管理界面

## 快速開始

### 1. 配置環境變數

複製範本並設定 Cloudflare API Token：

```bash
cd infra/deployment
cp .env.example .env
```

編輯 `.env` 文件，填入您的 Cloudflare API Token：

```bash
CLOUDFLARE_API_TOKEN=your_actual_token_here
```

**取得 API Token 步驟：**
1. 訪問 https://dash.cloudflare.com/profile/api-tokens
2. 點擊「Create Token」
3. 選擇「Create Custom Token」
4. 設定權限：
   - Account → Cloudflare Pages → Edit
   - Zone → DNS → Edit（選用）
5. 複製生成的 Token 並貼到 `.env` 文件

### 2. 啟動 CI/CD 容器

```bash
docker-compose up -d
```

### 3. 訪問管理界面

開啟瀏覽器訪問：http://localhost:3100

## API 文檔

### 檢查狀態

```bash
GET /api/status
```

**回應範例：**
```json
{
  "status": "running",
  "timestamp": "2025-11-03T12:00:00.000Z",
  "projects": ["admin-frontend", "shop-frontend"]
}
```

### 構建專案

```bash
POST /api/build/:project
```

**範例：**
```bash
curl -X POST http://localhost:3100/api/build/admin-frontend
```

**回應範例：**
```json
{
  "success": true,
  "project": "admin-frontend",
  "action": "build",
  "result": {
    "project": "admin-frontend",
    "timestamp": "2025-11-03T12:00:00.000Z",
    "distPath": "/dist/admin-frontend/dist"
  }
}
```

### 部署專案

```bash
POST /api/deploy/:project
Content-Type: application/json

{
  "skipBuild": false
}
```

**範例：**
```bash
curl -X POST http://localhost:3100/api/deploy/admin-frontend \
  -H "Content-Type: application/json" \
  -d '{"skipBuild": false}'
```

**回應範例：**
```json
{
  "success": true,
  "project": "admin-frontend",
  "action": "deploy",
  "buildResult": {
    "project": "admin-frontend",
    "timestamp": "2025-11-03T12:00:00.000Z",
    "distPath": "/dist/admin-frontend/dist"
  },
  "deployResult": {
    "project": "admin-frontend",
    "timestamp": "2025-11-03T12:05:00.000Z",
    "pagesUrl": "https://mimimart-admin.pages.dev",
    "customDomain": "https://mimimart-admin.xenolume.com"
  }
}
```

## 目錄結構

```
infra/deployment/
├── Dockerfile                    # CI/CD 容器映像
├── docker-compose.yml            # 容器配置
├── .gitignore                    # Git 忽略規則
├── README.md                     # 本文檔
├── server/                       # API 服務
│   ├── package.json
│   ├── index.js                  # Express API 入口
│   └── services/
│       ├── builder.js            # 構建邏輯
│       └── deployer.js           # 部署邏輯
├── config/                       # 專案配置
│   ├── admin-frontend.json
│   └── shop-frontend.json
├── web/                          # 管理前端界面
│   ├── index.html
│   └── style.css
├── dist/                         # 構建產物（掛載點）
│   ├── admin-frontend/
│   │   ├── dist/                 # 構建產物
│   │   ├── .build-info.json      # 構建資訊
│   │   └── .deploy-info.json     # 部署資訊
│   └── shop-frontend/
│       └── dist/
└── volumes/                      # 持久化卷（.gitignore）
    ├── .npm/                     # npm 快取
    └── .wrangler/                # Wrangler 登入狀態
```

## 新增前端專案

### 1. 建立配置文件

在 `config/` 目錄建立 `{project-name}.json`：

```json
{
  "pagesProjectName": "mimimart-xxx",
  "customDomain": "xxx.xenolume.com",
  "buildCommand": "npm run build:prod"
}
```

### 2. 建立產物目錄

```bash
mkdir -p dist/{project-name}
```

### 3. 重啟容器

```bash
docker-compose restart
```

### 4. 更新管理界面

在 `web/index.html` 中新增專案卡片。

## 常用命令

### 查看容器日誌

```bash
docker logs -f mimimart-ci-cd
```

### 重啟容器

```bash
cd infra/deployment
docker-compose restart
```

### 停止容器

```bash
docker-compose down
```

### 重新構建映像

```bash
docker-compose build --no-cache
docker-compose up -d
```

### 進入容器

```bash
docker exec -it mimimart-ci-cd sh
```

## 故障排查

### 構建失敗

1. 檢查源碼目錄是否存在：`docker exec mimimart-ci-cd ls /src`
2. 檢查 npm 依賴：查看容器日誌
3. 檢查構建命令：確認 `package.json` 中有 `build:prod` script

### 部署失敗

**錯誤：需要設定 CLOUDFLARE_API_TOKEN**

如果看到以下錯誤：
```
In a non-interactive environment, it's necessary to set a CLOUDFLARE_API_TOKEN environment variable
```

**解決方法：**
1. 確認 `.env` 文件存在並包含有效的 Token
2. 重啟容器：`docker-compose restart`
3. 檢查環境變數：`docker exec mimimart-ci-cd printenv | grep CLOUDFLARE`

**其他檢查：**
1. 檢查配置文件：`docker exec mimimart-ci-cd cat /app/config/admin-frontend.json`
2. 檢查構建產物：`docker exec mimimart-ci-cd ls /dist/admin-frontend/dist`
3. 測試 Wrangler：`docker exec mimimart-ci-cd npx wrangler whoami`

### API 無法訪問

1. 檢查容器是否運行：`docker ps | grep mimimart-ci-cd`
2. 檢查端口映射：`docker port mimimart-ci-cd`
3. 檢查容器日誌：`docker logs mimimart-ci-cd`

## 技術細節

### 源碼掛載

源碼目錄以唯讀方式掛載到容器：
- 主機：`../../src` → 容器：`/src:ro`
- 保護源碼不被構建流程修改

### 構建流程

1. 複製源碼到臨時工作區 `/tmp/build-{project}-{timestamp}`
2. 在工作區執行 `npm ci --cache /root/.npm`
3. 執行 `npm run build:prod`
4. 複製產物到 `/dist/{project}/dist`
5. 清理工作區

### 部署流程

1. 讀取 `/app/config/{project}.json` 配置
2. 檢查 `/dist/{project}/dist` 產物
3. 執行 `wrangler pages deploy`
4. 寫入部署資訊到 `.deploy-info.json`

### 持久化

- **npm 快取**：`volumes/.npm` → 加速依賴安裝
- **Wrangler 狀態**：`volumes/.wrangler` → 保存登入 Token

## 安全注意事項

1. **環境變數保護**：`.env` 文件已加入 `.gitignore`，切勿提交到版控
2. **API Token 安全**：
   - 使用最小權限原則建立 Cloudflare API Token
   - 僅授予必要的權限（Cloudflare Pages: Edit）
   - 定期輪換 Token
3. **源碼保護**：源碼目錄唯讀掛載，防止意外修改
4. **容器隔離**：所有構建與部署在容器內執行，與本機環境隔離
5. **持久化卷**：`volumes/` 目錄已加入 `.gitignore`，包含敏感的快取與登入狀態

## 優勢

✅ **環境完全隔離**：本機僅需 Docker，無需安裝 Node.js、Wrangler
✅ **源碼保護**：唯讀掛載，防止構建流程修改源碼
✅ **產物集中管理**：統一存放在 `infra/deployment/dist/`
✅ **Web 界面操作**：簡單直觀的管理頁面
✅ **API 驅動**：可輕鬆整合到其他系統
✅ **配置文件化**：每個專案獨立配置
✅ **持久化快取**：加速構建與保持登入狀態
✅ **可擴展**：支援多前端專案

## 未來擴展

- [ ] 新增後端部署支援
- [ ] 整合自動化測試
- [ ] 部署歷史記錄
- [ ] Webhook 通知（Slack/Discord）
- [ ] 多環境支援（dev/staging/prod）
- [ ] 回滾機制
