# CI/CD 系統

## 目標

容器化的前後端構建與部署系統，透過 Docker 隔離環境：
- **前端**: 使用 Cloudflare Pages 進行部署
- **後端**: 使用 Maven 打包 Spring Boot JAR，輸出到生產環境配置目錄

## 前置條件

- Docker 與 Docker Compose 已安裝
- （前端部署需要）擁有 Cloudflare 帳號與 API Token
- （後端打包需要）至少 3GB Docker 記憶體限制（Maven 構建需要）

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

### 前端構建

```bash
# 構建前端（僅構建，不部署）
curl -X POST http://localhost:3100/api/build/{project-name}

# 範例：構建商城前端
curl -X POST http://localhost:3100/api/build/shop-frontend
```

### 前端部署

```bash
# 完整部署（構建 + 部署到 Cloudflare Pages）
curl -X POST http://localhost:3100/api/deploy/{project-name} \
  -H "Content-Type: application/json" \
  -d '{"skipBuild": false}'

# 範例：部署管理後台
curl -X POST http://localhost:3100/api/deploy/admin-frontend \
  -H "Content-Type: application/json" \
  -d '{"skipBuild": false}'
```

### 後端打包

```bash
# 打包後端 JAR（輸出到 infra/prod-env/aws-ec2/backend/jar/app.jar）
curl -X POST http://localhost:3100/api/build/backend

# 首次構建需下載約 500-800MB 的 Maven 依賴，可能需要 3-5 分鐘
# 後續構建會使用快取，速度較快（約 1-2 分鐘）
```

### 網關部署

```bash
# 部署網關配置到 EC2（同步 Cloudflare Tunnel 和 Nginx 配置並重啟服務）
curl -X POST http://localhost:3100/api/deploy/gateway

# 需要先配置 config/gateway.json 文件
# 複製範本: cp config/gateway.example.json config/gateway.json
# 然後填入實際的 EC2 主機資訊和 SSH 金鑰路徑
```

### 檢查狀態

```bash
# 檢查系統狀態（包含 Java、Maven、rsync、SSH 環境檢測）
curl http://localhost:3100/api/status
```

## 配置說明

### 前端專案配置

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

### 網關部署配置

複製範本並填入實際資訊：

```bash
cd config
cp gateway.example.json gateway.json
```

編輯 `gateway.json`：

```json
{
  "ec2": {
    "host": "your-ec2-ip",
    "user": "ec2-user",
    "sshKeyPath": "/root/.ssh/your-ec2-key.pem"
  },
  "paths": {
    "cloudflaredConfig": "~/gateway/cloudflared/config/",
    "nginxConfig": "~/gateway/nginx/conf.d/"
  },
  "services": {
    "gateway": {
      "composeFile": "~/gateway/docker-compose.yml",
      "containers": ["nginx", "cloudflared"]
    }
  }
}
```

**重要**: `gateway.json` 已加入 `.gitignore`，不會被提交到版本控制。

## 驗證

1. 檢查容器運行：`docker ps | grep cicd-system`
2. 檢查管理界面是否可訪問
3. 查看容器日誌：`docker logs -f cicd-system`

## 後端部署到 AWS EC2

打包完成後，需要手動部署到 EC2：

```bash
# 1. 上傳 JAR 和配置到 EC2
scp -i ~/.ssh/your-ec2-key.pem \
    -r infra/prod-env/aws-ec2/backend \
    ec2-user@your-ec2-ip:~/

# 2. SSH 連線到 EC2
ssh -i ~/.ssh/your-ec2-key.pem ec2-user@your-ec2-ip

# 3. 啟動或重啟服務
cd ~/backend
docker-compose up -d

# 4. 查看日誌
docker logs -f mimimart-java
```

## 目錄結構

```
infra/deployment/
├── Dockerfile                          # Docker 映像配置（含 Node.js、JDK 21、Maven）
├── docker-compose.yml                  # 容器編排配置
├── .env                                # 環境變數（Cloudflare API Token）
├── server/                             # Express API 服務
│   ├── index.js                        # API 路由
│   ├── services/
│   │   ├── builder.js                  # 前端構建邏輯
│   │   ├── deployer.js                 # 前端部署邏輯
│   │   └── backend-builder.js          # 後端構建邏輯
│   └── package.json
├── config/                             # 專案配置（不上 Git）
│   ├── admin-frontend.json             # 管理後台配置
│   ├── shop-frontend.json              # 商城前端配置
│   ├── gateway.json                    # 網關部署配置（敏感）
│   ├── project.example.json            # 前端配置範本
│   └── gateway.example.json            # 網關配置範本
├── dist/                               # 前端構建產物
├── volumes/                            # 持久化卷
│   ├── .npm/                           # npm 快取
│   └── .wrangler/                      # Wrangler 登入狀態
└── web/                                # 管理界面
    ├── index.html
    └── style.css

注意：Maven 依賴快取共用開發環境的目錄（infra/dev-env/backend/volumes/.m2），
避免重複下載依賴，節省磁碟空間和構建時間。
```

## 網關配置部署流程

網關配置部署功能會自動執行以下步驟：

1. **同步配置文件到 EC2**
   - 使用 rsync 同步 `cloudflared/config/` 目錄
   - 使用 rsync 同步 `nginx/conf.d/` 目錄

2. **驗證 Nginx 配置**
   - 在 EC2 上執行 `nginx -t` 驗證配置語法

3. **重啟服務**
   - 重啟 `nginx` 和 `cloudflared` 容器
   - 等待服務啟動完成

4. **檢查容器狀態**
   - 返回容器運行狀態資訊

### 使用前提

- SSH 金鑰已掛載到容器（透過 `SSH_KEY_DIR` 環境變數）
- `config/gateway.json` 已正確配置
- EC2 主機可透過 SSH 連線
- EC2 上的 gateway 服務已初始化

## 完成

CI/CD 系統已啟動，可透過 Web 界面或 API 進行前後端構建與部署：
- **前端**: 自動構建並部署到 Cloudflare Pages
- **後端**: 打包 JAR 後需手動上傳到 EC2 部署
- **網關**: 自動同步配置到 EC2 並重啟服務
