package com.mimimart.application.service;

import com.mimimart.domain.member.exception.AccountDisabledException;
import com.mimimart.domain.member.exception.EmailAlreadyVerifiedException;
import com.mimimart.domain.member.exception.InvalidCredentialsException;
import com.mimimart.domain.member.exception.InvalidResetTokenException;
import com.mimimart.domain.member.exception.InvalidVerificationTokenException;
import com.mimimart.domain.member.exception.MemberAlreadyExistsException;
import com.mimimart.domain.member.exception.MemberNotFoundException;
import com.mimimart.domain.member.exception.PasswordMismatchException;
import com.mimimart.domain.member.exception.ResetTokenExpiredException;
import com.mimimart.domain.member.exception.VerificationTokenExpiredException;
import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.persistence.repository.RefreshTokenRepository;
import com.mimimart.shared.valueobject.MemberStatus;
import com.mimimart.shared.valueobject.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService 測試類別
 * 測試會員認證相關功能
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@DisplayName("會員認證服務測試")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailQuotaService emailQuotaService;

    @Autowired
    private TestFixtures fixtures;

    /**
     * 每個測試執行後清理 Redis 郵件配額計數器
     * 避免測試之間的配額累積導致測試失敗
     */
    @AfterEach
    void tearDown() {
        emailQuotaService.resetCurrentMonthQuota();
    }

    @Test
    @DisplayName("會員註冊 - 成功註冊新會員")
    void testRegister_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String name = "測試會員";

        // When
        AuthService.LoginResult result = authService.register(email, password, name);

        // Then - 驗證 Member 資料
        assertNotNull(result);
        assertNotNull(result.member);
        assertNotNull(result.member.getId());
        assertEquals(email, result.member.getEmail());
        assertEquals(name, result.member.getName());
        assertEquals(MemberStatus.ACTIVE, result.member.getStatus());
        assertFalse(result.member.getEmailVerified());
        assertNotNull(result.member.getPasswordHash());
        assertNotEquals(password, result.member.getPasswordHash()); // 密碼應該被加密
        assertNotNull(result.member.getVerificationToken());
        assertNotNull(result.member.getVerificationTokenExpiresAt());

        // Then - 驗證 Access Token
        assertNotNull(result.accessToken);
        assertTrue(result.accessToken.startsWith("eyJ")); // JWT 格式

        // Then - 驗證 Refresh Token
        assertNotNull(result.refreshToken);
        assertTrue(refreshTokenRepository.findByToken(result.refreshToken).isPresent());

        // Then - 驗證 lastLoginAt 設定為註冊時間
        assertNotNull(result.member.getLastLoginAt());
    }

    @Test
    @DisplayName("會員註冊 - Email 已存在應拋出例外")
    void testRegister_EmailExists() {
        // Given
        String email = "existing@example.com";
        authService.register(email, "password123", "現有會員");

        // When & Then
        assertThrows(MemberAlreadyExistsException.class, () -> {
            authService.register(email, "newpassword", "新會員");
        });
    }

    @Test
    @DisplayName("會員登入 - 使用正確帳密登入成功")
    void testLogin_Success() {
        // Given
        String email = "login@example.com";
        String password = "password123";
        authService.register(email, password, "登入測試會員");

        // When
        AuthService.LoginResult result = authService.login(email, password);

        // Then
        assertNotNull(result);
        assertNotNull(result.accessToken);
        assertNotNull(result.refreshToken);
        assertNotNull(result.member);
        assertEquals(email, result.member.getEmail());
        assertNotNull(result.member.getLastLoginAt());

        // 驗證 Refresh Token 已儲存
        assertTrue(refreshTokenRepository.findByToken(result.refreshToken).isPresent());
    }

    @Test
    @DisplayName("會員登入 - 錯誤的密碼應拋出例外")
    void testLogin_WrongPassword() {
        // Given
        String email = "wrongpw@example.com";
        authService.register(email, "correctpassword", "測試會員");

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(email, "wrongpassword");
        });
    }

    @Test
    @DisplayName("會員登入 - 不存在的 Email 應拋出例外")
    void testLogin_EmailNotExists() {
        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login("nonexistent@example.com", "password123");
        });
    }

    @Test
    @DisplayName("會員登出 - 成功撤銷所有 Refresh Token 並將 Access Token 加入黑名單")
    void testLogout_Success() {
        // Given
        String email = "logout@example.com";
        String password = "password123";
        AuthService.LoginResult registerResult = authService.register(email, password, "登出測試會員");
        AuthService.LoginResult loginResult = authService.login(email, password);

        // When: 登出時提供 Access Token
        authService.logout(registerResult.member.getId(), loginResult.accessToken);

        // Then: Refresh Token 應該被撤銷
        assertFalse(refreshTokenRepository.findByToken(loginResult.refreshToken).isPresent());
        // Note: Token Blacklist 的驗證在 TokenBlacklistServiceTest 中進行
    }

    @Test
    @DisplayName("Token 刷新 - 使用有效的 Refresh Token 取得新 Access Token")
    void testRefreshAccessToken_Success() {
        // Given
        String email = "refresh@example.com";
        String password = "password123";
        authService.register(email, password, "Token 刷新測試會員");
        AuthService.LoginResult loginResult = authService.login(email, password);
        String refreshToken = loginResult.refreshToken;

        // When
        AuthService.TokenRefreshResult refreshResult = authService.refreshAccessToken(refreshToken);

        // Then
        assertNotNull(refreshResult);
        assertNotNull(refreshResult.accessToken);
        // 驗證是有效的 JWT Token
        assertTrue(refreshResult.accessToken.startsWith("eyJ"));
    }

    @Test
    @DisplayName("Token 刷新 - 使用無效的 Refresh Token 應拋出例外")
    void testRefreshAccessToken_InvalidToken() {
        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.refreshAccessToken("invalid-refresh-token");
        });
    }

    @Test
    @DisplayName("Email 驗證 - 使用有效 Token 驗證成功")
    void testVerifyEmail_Success() {
        // Given - 使用 fixtures 建立未驗證會員
        Member member = fixtures.createTestMemberForEmailVerification(1);
        String token = member.getVerificationToken();

        // When
        authService.verifyEmail(token);

        // Then
        Member verifiedMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertTrue(verifiedMember.getEmailVerified());
        assertNull(verifiedMember.getVerificationToken());
        assertNull(verifiedMember.getVerificationTokenExpiresAt());
    }

    @Test
    @DisplayName("Email 驗證 - 使用無效 Token 應拋出例外")
    void testVerifyEmail_InvalidToken() {
        // When & Then
        assertThrows(InvalidVerificationTokenException.class, () -> {
            authService.verifyEmail("invalid-token");
        });
    }

    @Test
    @DisplayName("Email 驗證 - 已驗證的 Email 再次驗證應拋出例外")
    void testVerifyEmail_AlreadyVerified() {
        // Given - 使用 fixtures 建立未驗證會員
        Member member = fixtures.createTestMemberForEmailVerification(2);
        String token = member.getVerificationToken();
        authService.verifyEmail(token);

        // 重新取得會員的驗證 token（模擬重複驗證）
        Member verifiedMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();

        // When & Then - 因為已驗證，token 已被清空，應該拋出 InvalidVerificationTokenException
        assertThrows(InvalidVerificationTokenException.class, () -> {
            authService.verifyEmail(token);
        });
    }

    @Test
    @DisplayName("重新發送驗證郵件 - 成功")
    void testResendVerificationEmail_Success() {
        // Given - 使用 fixtures 建立未驗證會員
        Member member = fixtures.createTestMemberForEmailVerification(3);
        String oldToken = member.getVerificationToken();

        // When
        authService.resendVerificationEmail(member.getEmail());

        // Then
        Member updatedMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertNotNull(updatedMember.getVerificationToken());
        assertNotEquals(oldToken, updatedMember.getVerificationToken());
        assertNotNull(updatedMember.getVerificationTokenExpiresAt());
    }

    @Test
    @DisplayName("重新發送驗證郵件 - 會員不存在應拋出例外")
    void testResendVerificationEmail_MemberNotFound() {
        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            authService.resendVerificationEmail("nonexistent@example.com");
        });
    }

    @Test
    @DisplayName("重新發送驗證郵件 - 已驗證的 Email 應拋出例外")
    void testResendVerificationEmail_AlreadyVerified() {
        // Given - 使用 fixtures 建立未驗證會員，然後驗證它
        Member member = fixtures.createTestMemberForEmailVerification(4);
        authService.verifyEmail(member.getVerificationToken());

        // When & Then
        assertThrows(EmailAlreadyVerifiedException.class, () -> {
            authService.resendVerificationEmail(member.getEmail());
        });
    }

    @Test
    @DisplayName("申請密碼重設 - 成功")
    void testRequestPasswordReset_Success() {
        // Given
        String email = "reset@example.com";
        authService.register(email, "password123", "重設密碼會員");

        // When
        authService.requestPasswordReset(email);

        // Then
        Member member = memberRepository.findByEmail(email).orElseThrow();
        assertNotNull(member.getPasswordResetToken());
        assertNotNull(member.getPasswordResetTokenExpiresAt());
    }

    @Test
    @DisplayName("申請密碼重設 - 會員不存在應拋出例外")
    void testRequestPasswordReset_MemberNotFound() {
        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            authService.requestPasswordReset("nonexistent@example.com");
        });
    }

    @Test
    @DisplayName("重設密碼 - 成功")
    void testResetPassword_Success() {
        // Given
        String email = "password-reset@example.com";
        authService.register(email, "oldpassword", "重設密碼測試");
        authService.requestPasswordReset(email);
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String resetToken = member.getPasswordResetToken();
        String newPassword = "newpassword123";

        // When
        authService.resetPassword(resetToken, newPassword, newPassword);

        // Then
        Member updatedMember = memberRepository.findByEmail(email).orElseThrow();
        assertNull(updatedMember.getPasswordResetToken());
        assertNull(updatedMember.getPasswordResetTokenExpiresAt());

        // 驗證可以使用新密碼登入
        assertDoesNotThrow(() -> {
            authService.login(email, newPassword);
        });
    }

    @Test
    @DisplayName("重設密碼 - 密碼不一致應拋出例外")
    void testResetPassword_PasswordMismatch() {
        // Given
        String email = "mismatch@example.com";
        authService.register(email, "password123", "密碼不一致測試");
        authService.requestPasswordReset(email);
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String resetToken = member.getPasswordResetToken();

        // When & Then
        assertThrows(PasswordMismatchException.class, () -> {
            authService.resetPassword(resetToken, "newpassword", "differentpassword");
        });
    }

    @Test
    @DisplayName("重設密碼 - 使用無效 Token 應拋出例外")
    void testResetPassword_InvalidToken() {
        // When & Then
        assertThrows(InvalidResetTokenException.class, () -> {
            authService.resetPassword("invalid-token", "newpassword", "newpassword");
        });
    }
}
