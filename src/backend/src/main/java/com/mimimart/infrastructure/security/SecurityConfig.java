package com.mimimart.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 負責身份驗證、授權、密碼編碼和 JWT 認證
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 密碼編碼器 Bean
     * 使用 BCrypt 演算法,strength 10 (2^10 = 1024 輪)
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Security Filter Chain 配置
     * 定義端點權限和身份驗證規則
     *
     * 認證機制：
     * - 使用 JWT 無狀態認證（透過 Authorization Header: Bearer {token}）
     * - 會員 Access Token 有效期: 15 分鐘
     * - 管理員 Access Token 有效期: 30 分鐘
     * - Refresh Token 有效期: 7 天
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置異常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 停用 CSRF (純 API 專案,使用 JWT 認證)
            .csrf(csrf -> csrf.disable())

            // 啟用 CORS (使用 CorsConfig 中的配置)
            .cors(Customizer.withDefaults())

            // 無狀態 Session 管理 (使用 JWT,不建立 Session)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 授權規則
            .authorizeHttpRequests(auth -> auth
                // 公開端點: 前台會員認證 (註冊、登入、Email 驗證、密碼重設、Refresh Token)
                .requestMatchers("/api/shop/auth/**").permitAll()

                // 公開端點: 後台管理員認證 (登入、Refresh Token)
                .requestMatchers("/api/admin/auth/**").permitAll()

                // 公開端點: 測試資料端點 (僅開發/測試環境,由 @ConditionalOnProperty 控制)
                .requestMatchers("/api/test/**").permitAll()

                // 公開端點: Swagger UI 和 API 文件 (統一在 /swagger 路徑下)
                .requestMatchers("/swagger/**").permitAll()

                // 公開端點: 前台商品和分類瀏覽 (不需認證)
                .requestMatchers("/api/shop/product/**").permitAll()
                .requestMatchers("/api/shop/category/**").permitAll()

                // 前台需認證端點 (會員/地址)
                .requestMatchers("/api/shop/member/**").authenticated()
                .requestMatchers("/api/shop/address/**").authenticated()

                // 後台端點 (需認證)
                .requestMatchers("/api/admin/**").authenticated()

                // 其他端點需要認證
                .anyRequest().authenticated()
            )

            // 新增 JWT 認證過濾器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 停用表單登入 (使用 JSON API)
            .formLogin(form -> form.disable())

            // 停用 HTTP Basic Auth
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
