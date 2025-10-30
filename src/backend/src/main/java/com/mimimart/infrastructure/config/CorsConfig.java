package com.mimimart.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (跨來源資源共用) 配置
 * 允許前端從不同來源存取後端 API
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 配置 CORS 映射規則
     *
     * 允許的來源從環境變數讀取,支援多個來源 (逗號分隔)
     * 允許的方法: GET, POST (符合 MimiMart API 設計規範 - Constitution v1.2.1)
     * 允許的標頭: Content-Type, Authorization
     * 啟用憑證支援 (允許 Cookie 和 Authorization Header)
     * 預檢請求快取: 3600 秒 (1 小時)
     *
     * @param registry CORS 註冊器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
