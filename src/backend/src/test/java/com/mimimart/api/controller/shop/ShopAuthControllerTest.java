package com.mimimart.api.controller.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.api.dto.member.LoginRequest;
import com.mimimart.api.dto.member.RefreshTokenRequest;
import com.mimimart.api.dto.member.RegisterRequest;
import com.mimimart.api.dto.request.ForgotPasswordRequest;
import com.mimimart.api.dto.request.ResendVerificationEmailRequest;
import com.mimimart.api.dto.request.ResetPasswordRequest;
import com.mimimart.api.dto.request.VerifyEmailRequest;
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
 * ShopAuthController 測試類別
 * 測試會員認證 API 端點
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("會員認證 API 測試")
class ShopAuthControllerTest {

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
    @DisplayName("POST /api/shop/auth/register - 成功註冊新會員")
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newmember@example.com");
        request.setPassword("password123");
        request.setName("新會員");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/register")
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
    @DisplayName("POST /api/shop/auth/register - Email 已存在應返回失敗")
    void testRegister_EmailExists() throws Exception {
        // Given
        String email = "existing@example.com";
        authService.register(email, "password123", "現有會員");

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("newpassword");
        request.setName("新會員");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("此 Email 已被註冊"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/login - 使用正確帳密登入成功")
    void testLogin_Success() throws Exception {
        // Given
        String email = "login@example.com";
        String password = "password123";
        authService.register(email, password, "登入測試會員");

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        // When & Then
        mockMvc.perform(post("/api/shop/auth/login")
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
    @DisplayName("POST /api/shop/auth/login - 錯誤的密碼應返回失敗")
    void testLogin_WrongPassword() throws Exception {
        // Given
        String email = "wrongpw@example.com";
        authService.register(email, "correctpassword", "測試會員");

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email 或密碼錯誤"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/logout - 成功登出並將 Token 加入黑名單")
    void testLogout_Success() throws Exception {
        // Given: 先登入取得 Access Token
        String email = "logout.test@example.com";
        String password = "password123";
        authService.register(email, password, "登出測試會員");
        AuthService.LoginResult loginResult = authService.login(email, password);

        // When & Then: 使用 Access Token 登出
        mockMvc.perform(post("/api/shop/auth/logout")
                        .header("Authorization", "Bearer " + loginResult.accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/refresh-token - 使用有效的 Refresh Token 更新 Access Token")
    void testRefreshToken_Success() throws Exception {
        // Given
        String email = "refresh@example.com";
        String password = "password123";
        authService.register(email, password, "Token 刷新測試會員");
        AuthService.LoginResult loginResult = authService.login(email, password);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(loginResult.refreshToken);

        // When & Then
        mockMvc.perform(post("/api/shop/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token 更新成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("POST /api/shop/auth/refresh-token - 使用無效的 Refresh Token 應返回失敗")
    void testRefreshToken_InvalidToken() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh Token 無效或已過期"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/verify-email - 使用有效 Token 驗證 Email 成功")
    void testVerifyEmail_Success() throws Exception {
        // Given
        String email = "verify@example.com";
        Member member = authService.register(email, "password123", "驗證測試會員");
        String token = member.getVerificationToken();

        VerifyEmailRequest request = new VerifyEmailRequest(token);

        // When & Then
        mockMvc.perform(post("/api/shop/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email 驗證成功"))
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    @DisplayName("POST /api/shop/auth/verify-email - 使用無效 Token 應返回失敗")
    void testVerifyEmail_InvalidToken() throws Exception {
        // Given
        VerifyEmailRequest request = new VerifyEmailRequest("invalid-token");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無效的驗證 Token"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/resend-verification - 重新發送驗證郵件成功")
    void testResendVerificationEmail_Success() throws Exception {
        // Given
        String email = "resend@example.com";
        authService.register(email, "password123", "重發測試會員");

        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest(email);

        // When & Then
        mockMvc.perform(post("/api/shop/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("驗證郵件已重新發送"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/resend-verification - 會員不存在應返回失敗")
    void testResendVerificationEmail_MemberNotFound() throws Exception {
        // Given
        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest("nonexistent@example.com");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("會員不存在"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/forgot-password - 申請密碼重設成功")
    void testForgotPassword_Success() throws Exception {
        // Given
        String email = "forgot@example.com";
        authService.register(email, "password123", "忘記密碼測試");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        // When & Then
        mockMvc.perform(post("/api/shop/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密碼重設郵件已發送"))
                .andExpect(jsonPath("$.data.message").value("密碼重設郵件已發送，請查看您的信箱"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/forgot-password - 會員不存在應返回失敗")
    void testForgotPassword_MemberNotFound() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("會員不存在"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/reset-password - 重設密碼成功")
    void testResetPassword_Success() throws Exception {
        // Given
        String email = "reset@example.com";
        authService.register(email, "oldpassword", "重設密碼測試");
        authService.requestPasswordReset(email);
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String resetToken = member.getPasswordResetToken();

        ResetPasswordRequest request = new ResetPasswordRequest(resetToken, "newpassword123", "newpassword123");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密碼重設成功"))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("POST /api/shop/auth/reset-password - 密碼不一致應返回失敗")
    void testResetPassword_PasswordMismatch() throws Exception {
        // Given
        String email = "mismatch@example.com";
        authService.register(email, "password123", "密碼不一致測試");
        authService.requestPasswordReset(email);
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String resetToken = member.getPasswordResetToken();

        ResetPasswordRequest request = new ResetPasswordRequest(resetToken, "newpassword", "differentpassword");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("新密碼與確認密碼不一致"));
    }

    @Test
    @DisplayName("POST /api/shop/auth/reset-password - 使用無效 Token 應返回失敗")
    void testResetPassword_InvalidToken() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "newpassword", "newpassword");

        // When & Then
        mockMvc.perform(post("/api/shop/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無效的密碼重設 Token"));
    }
}
