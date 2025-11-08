package com.mimimart.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

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
    )
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

    /**
     * 自訂 OpenAPI 配置
     *
     * <p>從環境變數動態讀取 Base URL,避免硬編碼端口號</p>
     *
     * @param baseUrl 應用基礎 URL (從 application.yml 的 app.base-url 讀取)
     * @return OpenAPI 配置物件
     */
    @Bean
    public OpenAPI customOpenAPI(@Value("${app.base-url}") String baseUrl) {
        return new OpenAPI()
            .servers(Arrays.asList(
                new io.swagger.v3.oas.models.servers.Server()
                    .url(baseUrl)
                    .description("當前環境"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("https://api.mimimart.com")
                    .description("生產環境")
            ));
    }
}
