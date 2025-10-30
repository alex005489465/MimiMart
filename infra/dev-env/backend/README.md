# MimiMart Java 後端開發環境

Java 21 + Spring Boot 開發容器

## 快速開始

```bash
docker-compose up -d
```

## 服務資訊

- 容器: mimimart-java (maven:3.9.11-eclipse-temurin-21)
- 端口: 8083 (Spring Boot), 5005 (Debug)
- 源碼: `src/backend/backend-2`

## 環境配置

```bash
cp .env.example .env
```

**注意**: `.env` 僅含基礎設施連接資訊 (MySQL、MinIO),應用配置在 `application.yml`。

## 常用指令

```bash
# 進入容器
docker exec mimimart-java bash

# 查看日誌
docker-compose logs -f java-dev

# 停止服務
docker-compose down
```

## 注意事項

1. 需先啟動 database 模組 (等待約 30 秒)
2. 確保端口 8083 和 5005 未被占用
