# MimiMart 電商前端

## 目標

使用 React 19 + Vite 建構的 MimiMart 電商平台前端應用程式。

## 技術架構

- **框架**: React 19
- **建構工具**: Vite
- **語言**: JavaScript (ES6+)
- **樣式**: CSS Modules
- **路由**: React Router v7
- **容器**: Docker (Node 24-alpine)

## 前置條件

- Node.js 24 或以上
- Docker 和 Docker Compose (推薦使用容器開發)
- npm 或 yarn

## 執行步驟

### 1. 使用 Docker(推薦)

啟動容器並安裝依賴:
```bash
cd infra/dev-env/frontend
docker-compose up -d
docker exec mimimart-shop-frontend npm install
```

啟動開發伺服器:
```bash
docker exec mimimart-shop-frontend npm run dev
```

訪問應用: http://localhost:5173

停止容器:
```bash
docker-compose down
```

### 2. 本機開發

安裝依賴:
```bash
cd src/shop-frontend
npm install
```

設定環境變數:
```bash
cp .env.example .env
```

需要的環境變數(請參考 `.env.example`):
- `VITE_API_BASE_URL`: 後端 API 位址
- `VITE_APP_ENV`: 執行環境
- `VITE_APP_TITLE`: 應用程式名稱

啟動開發伺服器:
```bash
npm run dev
```

### 3. 建構生產版本

```bash
npm run build
```

預覽建構結果:
```bash
npm run preview
```

## 專案結構

```
src/shop-frontend/
├── public/              # 靜態資源
├── src/
│   ├── components/      # React 元件
│   ├── pages/           # 頁面元件
│   ├── App.jsx          # 主應用元件(路由配置)
│   ├── main.jsx         # 入口檔案
│   └── index.css        # 全域樣式
├── .env.example         # 環境變數範本
├── index.html           # HTML 模板
├── package.json         # 依賴配置
└── vite.config.js       # Vite 配置
```

## 可用指令

- `npm run dev`: 啟動開發伺服器(支援熱更新)
- `npm run build`: 建構生產版本
- `npm run preview`: 預覽建構後的版本

## 驗證

開發伺服器成功啟動後:
1. 瀏覽器訪問 http://localhost:5173
2. 應看到 MimiMart 首頁,包含 Header 導航列和 Hero Banner 輪播廣告
3. 控制台無錯誤訊息

## 完成

MimiMart 電商前端應用程式已成功設定並運行。
