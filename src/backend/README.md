# MimiMart Backend

MimiMart 電商平台後端服務 - 會員與管理員系統

## 專案資訊

- **版本**: 1.0.0
- **Java**: 21
- **Spring Boot**: 3.5.0
- **構建工具**: Maven 3.9+
- **資料庫**: MySQL 8.4+

## 技術棧

### 核心框架
- Spring Boot 3.5.0
- Spring Security (JWT 認證)
- Spring Data JPA
- Flyway (資料庫遷移)

### 主要依賴
- MySQL Connector/J 9.1.0
- JJWT 0.12.6 (JWT 實作)
- SpringDoc OpenAPI 2.7.0 (Swagger UI)
- Spring Mail + Thymeleaf (郵件服務)
- Lombok
- Jakarta Validation

## 專案架構

### DDD 分層架構

```
src/main/java/com/mimimart/
├── api/                    # API 層
│   ├── controller/         # REST 控制器
│   │   ├── admin/         # 後台管理員控制器
│   │   └── storefront/    # 前台會員控制器
│   └── dto/               # 資料傳輸物件
│
├── application/           # 應用服務層
│   ├── service/          # 應用服務
│   └── event/            # 事件處理器
│
├── domain/               # 領域層
│   ├── admin/           # 管理員領域
│   └── member/          # 會員領域
│
├── infrastructure/      # 基礎設施層
│   ├── config/         # 配置類別
│   ├── persistence/    # 持久化
│   │   ├── entity/    # JPA 實體
│   │   ├── repository/# JPA Repository
│   │   └── mapper/    # 領域模型 <-> Entity 映射器
│   └── security/      # 安全認證
│
├── shared/            # 共用模組
│   ├── valueobject/  # 值對象
│   ├── validation/   # 驗證邏輯
│   └── exception/    # 領域異常基礎類別
│
└── exception/        # 全域異常處理
```

## 已實作功能

### 1. 基礎架構 ✅
- Maven 專案配置 (pom.xml)
- DDD 分層套件結構
- 統一配置檔 (application.yml)
- 環境變數配置 (.env.example)

### 2. 資料庫層 ✅
**Flyway 遷移檔案:**
- V1: `members` 資料表 (會員基本資訊、Email 驗證、密碼重設)
- V2: `member_addresses` 資料表 (收貨地址)
- V3: `admins` 資料表 (管理員,簡化版)
- V4: `refresh_tokens` 資料表 (雙用戶支援)

**資料庫設計原則:**
- 零約束設計:無外鍵、無 UNIQUE 約束 (除主鍵)、無 CHECK 約束
- 應用程式層負責完整性驗證

### 3. 認證與安全 ✅
- JWT 工具類 (JwtUtil)
- JWT 認證過濾器 (JwtAuthenticationFilter)
- Spring Security 配置 (SecurityConfig)
- 密碼編碼 (BCrypt)
- 自訂 UserDetailsService (雙用戶支援)

### 4. Entity & Repository ✅
**Entity:**
- Member (會員)
- MemberAddress (收貨地址)
- Admin (管理員)
- RefreshToken (Refresh Token)

**Repository:**
- MemberRepository
- MemberAddressRepository
- AdminRepository
- RefreshTokenRepository

### 5. 共用模組 ✅
- ApiResponse (統一 API 回應格式)
- GlobalExceptionHandler (全域異常處理)
- 領域異常類別

### 6. Service 層 ✅
- **RefreshTokenService**: Refresh Token 管理
- **AuthService**: 會員註冊、登入、登出、Token 更新
- **MemberService**: 會員資料管理
- **AddressService**: 收貨地址管理
- **AdminService**: 管理員登入、登出、Token 更新

### 7. Controller 層 ✅
**前台會員:**
- StorefrontAuthController (註冊/登入/登出/更新Token)
- StorefrontMemberController (查看/更新資料/修改密碼)
- StorefrontAddressController (地址管理)

**後台管理員:**
- AdminAuthController (登入/登出/更新Token)

### 8. DTO 層 ✅
**會員相關:**
- RegisterRequest, LoginRequest, LoginResponse
- MemberProfile, UpdateProfileRequest, ChangePasswordRequest
- RefreshTokenRequest
- AddressRequest, AddressResponse

**管理員相關:**
- AdminLoginRequest, AdminLoginResponse
- AdminProfile

## API 設計規範

本專案遵循 **Constitution v1.2.1**:

### HTTP 方法限制
- **僅允許 GET 和 POST**
- 變更操作語意化:`/create`, `/{id}/update`, `/{id}/delete`

### 統一回應格式
```json
{
  "success": boolean,
  "code": string,
  "message": string,
  "data": object,
  "meta": object
}
```

### 錯誤處理標準
- **HTTP 200**:所有業務處理完成的請求
- 透過 `success: true/false` 區分業務成功/失敗
- HTTP 非 200:僅用於基礎設施問題

## 快速開始

### 前置需求
- Docker & Docker Compose
- MySQL 8.4+ (或透過 Docker 容器運行)
- Java 21
- Maven 3.9+

### 環境設定

1. 複製環境變數範本:
```bash
cp .env.example .env
```

2. 編輯 `.env` 檔案,設定以下變數:
```bash
# 資料庫配置
DB_URL=jdbc:mysql://<db-host>:3306/<database-name>
DB_USERNAME=<your-db-username>
DB_PASSWORD=<your-db-password>

# JWT 配置
JWT_SECRET=<your-jwt-secret-key-at-least-512-bits>
JWT_MEMBER_ACCESS_EXPIRATION=900000
JWT_ADMIN_ACCESS_EXPIRATION=1800000
JWT_REFRESH_EXPIRATION=604800000

# 郵件配置
MAIL_HOST=<smtp-host>
MAIL_PORT=<smtp-port>
MAIL_USERNAME=<your-email>
MAIL_PASSWORD=<your-email-password>

# 應用程式配置
APP_BASE_URL=http://localhost:8080
```

### 啟動應用程式

#### 使用 Docker Compose (推薦)

1. 啟動開發環境:
```bash
cd infra/backend
docker-compose up -d
```

2. 進入容器:
```bash
docker exec -it <container-name> bash
```

3. 編譯並運行:
```bash
mvn clean install
mvn spring-boot:run
```

#### 本地運行

1. 確保 MySQL 服務已啟動

2. 編譯專案:
```bash
mvn clean install
```

3. 運行應用程式:
```bash
mvn spring-boot:run
```

### 訪問應用程式

- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

## 測試資料

專案包含測試資料遷移檔案 (`R__insert_sample_data.sql`),提供測試用的會員和管理員帳號。

**注意**: 測試帳號密碼請參考 `CLAUDE.md` 檔案 (僅限開發團隊)。

## 專案狀態

**✅ 100% 完成** - 核心會員與管理員系統已完整實作!

### 可選的後續擴充功能
1. Email 驗證服務
2. 密碼重設服務
3. 郵件範本
4. 定時任務 (清理過期 Token)
5. API 文件完善

## 參考文件

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.5.0/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Flyway Documentation](https://flywaydb.org/documentation/)

## 授權

© 2025 MimiMart Development Team
