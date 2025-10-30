package com.mimimart.infrastructure.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Swagger UI 客製化配置
 *
 * <p>負責配置 Swagger 相關資源和路徑,並注入自訂主題腳本</p>
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerUiCustomizer implements WebMvcConfigurer {

    /**
     * 註冊靜態資源處理器
     * 將 /swagger/** 路徑映射到 classpath:/static/swagger/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger/css/**", "/swagger/js/**")
                .addResourceLocations("classpath:/static/swagger/css/", "classpath:/static/swagger/js/");
    }

    /**
     * 配置視圖控制器
     * 將 /swagger 重定向到 Swagger UI
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger", "/swagger/ui.html");
    }

    /**
     * 自訂 Swagger UI index.html 轉換器
     * 注入自訂的 JavaScript 以支援暗色主題自動切換
     */
    @Bean
    public SwaggerIndexPageTransformer swaggerIndexPageTransformer(
            SwaggerUiConfigProperties swaggerUiConfigProperties,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {

        return new SwaggerIndexPageTransformer(
                swaggerUiConfigProperties,
                swaggerUiOAuthProperties,
                swaggerWelcomeCommon,
                objectMapperProvider) {

            @Override
            public Resource transform(HttpServletRequest request, Resource resource,
                                    ResourceTransformerChain transformer) throws IOException {

                // 只處理 index.html
                if (resource.getFilename() != null && resource.getFilename().equals("index.html")) {
                    try (InputStream is = resource.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                         BufferedReader br = new BufferedReader(isr)) {

                        StringBuilder htmlBuilder = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            // 在 </head> 標籤前注入自訂的 JavaScript
                            if (line.contains("</head>")) {
                                htmlBuilder.append("  <!-- MimiMart 自訂暗色主題 -->\n");
                                htmlBuilder.append("  <script src=\"/swagger/js/theme-detector.js\"></script>\n");
                            }
                            htmlBuilder.append(line).append("\n");
                        }

                        return new TransformedResource(resource, htmlBuilder.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                return super.transform(request, resource, transformer);
            }
        };
    }
}
