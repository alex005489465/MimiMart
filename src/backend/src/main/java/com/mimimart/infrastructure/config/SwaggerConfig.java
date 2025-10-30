package com.mimimart.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 文件配置
 *
 * <p>配置 API 文件的基本資訊、安全認證方式等</p>
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "MimiMart API",
        version = "1.0.0",
        description = """
            MimiMart 電商平台 RESTful API 文件

            ## 功能模組
            - 會員系統 (註冊、登入、個人資訊管理)
            - 管理員系統 (管理員登入、權限管理)
            - 身份驗證 (JWT Token 認證)

            ## 認證方式
            大部分 API 需要在 Header 中攜帶 JWT Token:
            ```
            Authorization: Bearer {your-jwt-token}
            ```

            ## 回應格式
            所有 API 回應皆採用統一的 JSON 格式。
            """,
        contact = @Contact(
            name = "MimiMart 開發團隊",
            email = "dev@mimimart.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "本地開發環境",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "生產環境",
            url = "https://api.mimimart.com"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    description = "JWT 認證方式。請在下方輸入框中輸入 JWT Token (不需要加 'Bearer ' 前綴)",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // 此類別透過註解進行配置,無需額外的 Bean 定義
}
