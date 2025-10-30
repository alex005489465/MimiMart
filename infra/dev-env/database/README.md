# MimiMart Database 服務

提供 MySQL 8、Redis、phpMyAdmin 及 RedisInsight 的 Docker 容器化環境。

## 快速開始

### 1. 環境變數設定
```bash
cp .env.example .env
# 編輯 .env 設定密碼
```

### 2. 啟動服務
```bash
# 先啟動共享網路
cd ../shared && docker-compose up -d

# 啟動資料庫服務
cd ../database && docker-compose up -d
```

### 3. 管理介面
- **phpMyAdmin**: http://localhost:8081 (root 帳號，密碼見 `.env`)
- **RedisInsight**: http://localhost:5540

## 連線資訊

| 項目 | 值 |
|------|------|
| 資料庫名稱 | `mimimart_ecommerce` |
| MySQL 連線 | `mysql:3306` |
| Redis 連線 | `redis:6379` |
| 使用者/密碼 | 查看 `.env` |


## 常用指令

### 查看服務狀態
```bash
docker-compose ps
docker-compose logs --tail=50 mysql
```

### 連接資料庫
```bash
docker-compose exec -T mysql mysql -u root -p$(grep MYSQL_ROOT_PASSWORD .env | cut -d '=' -f2) mimimart_ecommerce
```

### 備份與還原
```bash
# 備份
docker-compose exec -T mysql mysqldump -u root -p$(grep MYSQL_ROOT_PASSWORD .env | cut -d '=' -f2) mimimart_ecommerce > backup_$(date +%Y%m%d_%H%M%S).sql

# 還原
docker-compose exec -T mysql mysql -u root -p$(grep MYSQL_ROOT_PASSWORD .env | cut -d '=' -f2) mimimart_ecommerce < backup.sql
```

## 注意事項

1. **資料持久化**：資料儲存在 `volumes/` 目錄，`docker-compose down` 不會刪除
2. **密碼安全**：不要提交 `.env` 到版本控制
3. **Port 衝突**：確保 8081 (phpMyAdmin) 和 5540 (RedisInsight) 未被佔用
