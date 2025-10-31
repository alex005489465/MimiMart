package com.mimimart.api.controller.storefront;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.api.dto.member.LoginRequest;
import com.mimimart.api.dto.member.RefreshTokenRequest;
import com.mimimart.api.dto.member.RegisterRequest;
import com.mimimart.application.service.AuthService;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StorefrontAuthController 測試類別
 * 測試會員認證 API 端點
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("會員認證 API 測試")
class StorefrontAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // 清理可能的測試資料
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/storefront/auth/register - 成功註冊新會員")
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newmember@example.com");
        request.setPassword("password123");
        request.setName("新會員");

        // When & Then
        mockMvc.perform(post("/api/storefront/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("註冊成功"))
                .andExpect(jsonPath("$.data.email").value("newmember@example.com"))
                .andExpect(jsonPath("$.data.name").value("新會員"))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("POST /api/storefront/auth/register - Email 已存在應返回失敗")
    void testRegister_EmailExists() throws Exception {
        // Given
        String email = "existing@example.com";
        authService.register(email, "password123", "現有會員");

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("newpassword");
        request.setName("新會員");

        // When & Then
        mockMvc.perform(post("/api/storefront/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("此 Email 已被註冊"));
    }

    @Test
    @DisplayName("POST /api/storefront/auth/login - 使用正確帳密登入成功")
    void testLogin_Success() throws Exception {
        // Given
        String email = "login@example.com";
        String password = "password123";
        authService.register(email, password, "登入測試會員");

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        // When & Then
        mockMvc.perform(post("/api/storefront/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登入成功"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.profile.email").value(email))
                .andExpect(jsonPath("$.data.profile.name").value("登入測試會員"));
    }

    @Test
    @DisplayName("POST /api/storefront/auth/login - 錯誤的密碼應返回失敗")
    void testLogin_WrongPassword() throws Exception {
        // Given
        String email = "wrongpw@example.com";
        authService.register(email, "correctpassword", "測試會員");

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/storefront/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email 或密碼錯誤"));
    }

    @Test
    @DisplayName("POST /api/storefront/auth/logout - 成功登出")
    void testLogout_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/storefront/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    @DisplayName("POST /api/storefront/auth/refresh-token - 使用有效的 Refresh Token 更新 Access Token")
    void testRefreshToken_Success() throws Exception {
        // Given
        String email = "refresh@example.com";
        String password = "password123";
        authService.register(email, password, "Token 刷新測試會員");
        AuthService.LoginResult loginResult = authService.login(email, password);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(loginResult.refreshToken);

        // When & Then
        mockMvc.perform(post("/api/storefront/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token 更新成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("POST /api/storefront/auth/refresh-token - 使用無效的 Refresh Token 應返回失敗")
    void testRefreshToken_InvalidToken() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        // When & Then
        mockMvc.perform(post("/api/storefront/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh Token 無效或已過期"));
    }
}
