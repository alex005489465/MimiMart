# MimiMart 開發環境

## 服務架構

本專案採用模組化的 Docker Compose 配置，各服務獨立管理：

```
infra/
├── database/     # MySQL + Redis + 管理工具
└── backend/      # Java 21 + Spring Boot
```

## 網路架構

所有服務自動加入 `mimimart-network` 網路，**無需啟動順序**：

- 第一個啟動的模組會自動創建 `mimimart-network`
- 後續啟動的模組會自動加入已存在的網路
- 所有容器可透過容器名稱互相通訊（如 `mimimart-mysql`, `mimimart-redis`, `mimimart-java` 等）

## 快速啟動

**可任意順序啟動**，無需擔心依賴關係：

```bash
# 啟動資料庫服務
cd database
docker-compose up -d

# 啟動 Java 後端服務
cd ../backend
docker-compose up -d
```

或反過來也可以：

```bash
# 先啟動後端
cd backend
docker-compose up -d

# 再啟動資料庫
cd ../database
docker-compose up -d
```

## 停止服務

停止任意模組：

```bash
# 停止後端
cd backend && docker-compose down

# 停止資料庫
cd ../database && docker-compose down
```

## 服務說明

### database/ - 資料庫服務
- MySQL (Port 3306，內部網路)
- phpMyAdmin (Port 8081)
- Redis (Port 6379，內部網路)
- RedisInsight (依環境變數配置)

### backend/ - Java 後端服務
- Spring Boot (Port 8083)
- JPDA Debug (Port 5005)

## 端口總覽

| 服務 | 端口 | 說明 |
|------|------|------|
| phpMyAdmin | 8081 | MySQL 管理工具 |
| Spring Boot | 8083 | Java API |
| Java Debug | 5005 | 遠程除錯 |
| RedisInsight | 動態 | Redis 管理工具 (依 .env 配置) |
| MySQL | 3306 | 內部網路 |
| Redis | 6379 | 內部網路 |

## 注意事項

- 所有服務自動共用 `mimimart-network` 網路，可任意順序啟動
- 容器透過容器名稱互相通訊（如 `mimimart-mysql`, `mimimart-redis`, `mimimart-java`）
- 首次啟動 backend/ 後需要初始化 Spring Boot 專案（參考 backend/README.md）
- database/ 需要設定 `.env` 檔案（參考 database/README.md）
