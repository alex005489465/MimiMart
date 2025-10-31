# MimiMart 管理前端

## 目標

MimiMart 電商平台的管理後台前端應用程式，使用 React 19 + Vite 構建。

## 技術棧

- React 19
- Vite 6
- React Router 7
- Axios
- CSS Modules

## 前置條件

- Docker 已安裝並運行
- 後端 API 服務已啟動（預設端口 8083）

## 執行步驟

### 1. 啟動容器

```bash
# 進入前端開發環境目錄
cd infra/dev-env/frontend

# 啟動容器
docker-compose up -d
```

### 2. 安裝依賴

```bash
docker exec mimimart-admin-frontend npm install
```

### 3. 配置環境變數

```bash
# 複製環境變數範本
docker exec mimimart-admin-frontend cp .env.example .env
```

需要的環境變數：
- `VITE_API_BASE_URL` - 後端 API 位址
- `VITE_APP_ENV` - 應用程式環境
- `VITE_APP_TITLE` - 應用程式標題

### 4. 啟動開發伺服器

```bash
docker exec mimimart-admin-frontend npm run dev
```

### 5. 訪問應用

開啟瀏覽器訪問：http://localhost:5174

## 驗證

- 瀏覽器能正常開啟管理後台登入頁面
- 無控制台錯誤

## 目錄結構

```
src/
├── components/      # UI 元件
├── pages/          # 頁面元件
│   ├── Login/      # 登入頁面
│   └── Dashboard/  # 儀表板
├── services/       # API 服務
├── utils/          # 工具函數
├── App.jsx         # 路由配置
└── main.jsx        # 應用入口
```

## 可用指令

```bash
# 開發模式
docker exec mimimart-admin-frontend npm run dev

# 建構生產版本
docker exec mimimart-admin-frontend npm run build

# 預覽生產版本
docker exec mimimart-admin-frontend npm run preview
```

## 開發規範

詳細的開發規範、路徑配置和疑難排解請參考同目錄的 `CLAUDE.md` 文件。

## 完成

管理前端開發環境已就緒，可以開始開發功能。
