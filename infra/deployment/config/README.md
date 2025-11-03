# 專案配置說明

## 配置結構

每個前端專案需要一個 JSON 配置文件：`{project-name}.json`

```json
{
  "pagesProjectName": "cloudflare-pages-專案名稱",
  "customDomain": "自訂域名",
  "buildCommand": "npm run build:prod"
}
```

## 對應關係

| 項目 | 規則 | 範例 |
|------|------|------|
| 配置文件名 | `{project-name}.json` | `admin-frontend.json` |
| 源碼目錄 | `src/{project-name}/` | `src/admin-frontend/` |
| 產物目錄 | `dist/{project-name}/dist/` | `dist/admin-frontend/dist/` |
| API 請求 | `/api/deploy/{project-name}` | `/api/deploy/admin-frontend` |

**重要**：配置文件名 = 源碼目錄名 = API 專案名稱（必須一致）

## 現有專案

- `admin-frontend.json` → 管理後台
- `shop-frontend.json` → 商城前端

## 新增專案

1. 建立配置文件：`config/{project-name}.json`
2. 確保 `src/{project-name}/` 存在
3. 重啟容器：`docker-compose restart`
4. 更新管理界面（可選）
