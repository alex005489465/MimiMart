# CI/CD 系統

## 目標

容器化的前端構建與部署系統，透過 Docker 隔離環境，使用 Cloudflare Pages 進行部署。

## 前置條件

- Docker 與 Docker Compose 已安裝
- 擁有 Cloudflare 帳號與 API Token

## 執行步驟

### 1. 配置環境變數

複製範本並填入您的 Cloudflare API Token：

```bash
cd infra/deployment
cp .env.example .env
```

編輯 `.env` 文件：

```bash
CLOUDFLARE_API_TOKEN=<YOUR_CLOUDFLARE_API_TOKEN>
```

取得 API Token：
1. 訪問 https://dash.cloudflare.com/profile/api-tokens
2. 建立 Custom Token，設定權限：Account → Cloudflare Pages → Edit

### 2. 啟動容器

```bash
docker-compose up -d
```

### 3. 訪問管理界面

開啟瀏覽器：http://localhost:3100

## 使用 API

### 構建專案

```bash
curl -X POST http://localhost:3100/api/build/{project-name}
```

### 部署專案

```bash
curl -X POST http://localhost:3100/api/deploy/{project-name} \
  -H "Content-Type: application/json" \
  -d '{"skipBuild": false}'
```

### 檢查狀態

```bash
curl http://localhost:3100/api/status
```

## 新增專案

在 `config/` 目錄建立配置文件 `{project-name}.json`：

```json
{
  "pagesProjectName": "your-pages-project",
  "customDomain": "your-domain.example.com",
  "buildCommand": "npm run build:prod"
}
```

建立產物目錄後重啟容器：

```bash
mkdir -p dist/{project-name}
docker-compose restart
```

## 驗證

1. 檢查容器運行：`docker ps | grep cicd-system`
2. 檢查管理界面是否可訪問
3. 查看容器日誌：`docker logs -f cicd-system`

## 完成

CI/CD 系統已啟動，可透過 Web 界面或 API 進行構建與部署。
