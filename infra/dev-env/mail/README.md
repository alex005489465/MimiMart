# Mailpit 郵件測試服務

## 目標

提供本地開發環境的 SMTP 郵件測試工具,攔截並顯示應用程式發送的測試郵件。

## 前置條件

- Docker & Docker Compose 已安裝
- 共用網路 `mimimart-network` 已建立

## 執行步驟

### 1. 環境設定

複製環境變數範本並檢查配置:

```bash
cp .env.example .env
```

預設配置:
- 最大郵件數量: 500 封
- 接受任何 SMTP 認證
- 允許不安全的認證

### 2. 啟動服務

```bash
docker-compose up -d
```

### 3. 訪問 Web UI

開啟瀏覽器訪問:
```
http://localhost:8025
```

## 應用程式整合

### Spring Boot 配置

在 `application.yml` 或 `application-dev.yml` 中配置:

```yaml
spring:
  mail:
    host: mailpit          # 容器名稱
    port: 1025             # SMTP port
    username: ""           # 不需要認證
    password: ""
    properties:
      mail:
        smtp:
          auth: false
          starttls:
            enable: false
```

### 測試郵件發送

```java
@Autowired
private JavaMailSender mailSender;

public void sendTestEmail() {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo("test@example.com");
    message.setSubject("測試郵件");
    message.setText("測試內容");
    mailSender.send(message);
}
```

發送後立即在 Web UI 查看郵件。

## 驗證

### 檢查服務狀態

```bash
docker-compose ps
```

應該看到 `mailpit` 容器狀態為 `running`。

### 測試連線

從應用容器測試 SMTP 連線:

```bash
docker exec -it <your-app-container> bash
telnet mailpit 1025
```

成功連線會看到 `220 Mailpit` 回應。

### 發送測試郵件

從應用程式發送測試郵件後,訪問 `http://localhost:8025` 應該能看到郵件列表。

## 功能特性

- **安全測試**: 不會真的發送郵件到外部
- **HTML 支援**: 可查看 HTML 郵件與 MIME 結構
- **搜尋功能**: 提供郵件搜尋與過濾
- **API 存取**: 支援透過 API 存取郵件資料

## 疑難排解

### Web UI 無法訪問

檢查容器狀態:
```bash
docker-compose ps mailpit
docker-compose logs --tail=50 mailpit
```

### 應用程式無法發送郵件

檢查清單:
1. Mailpit 容器是否運行?
2. 應用容器與 Mailpit 是否在同一網路?
3. SMTP 配置是否使用 `mailpit:1025`?
4. 應用容器能否 ping 通 mailpit?

測試網路連線:
```bash
docker network inspect mimimart-network
```

應該看到應用容器和 `mimimart-mailpit` 都在列表中。

### Port 8025 被佔用

修改 `docker-compose.yml` 的端口映射:
```yaml
ports:
  - "8026:8025"  # 改用 8026
```

重啟服務後訪問 `http://localhost:8026`

## 完成

Mailpit 服務已啟動,可透過 `mailpit:1025` 接收測試郵件,並在 `http://localhost:8025` 查看。
