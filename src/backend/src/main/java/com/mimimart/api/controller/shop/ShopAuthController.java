package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.member.*;
import com.mimimart.api.dto.request.*;
import com.mimimart.api.dto.response.*;
import com.mimimart.application.service.AuthService;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 前台會員認證 Controller
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/shop/auth")
@Tag(name = "會員認證", description = "會員註冊、登入、登出相關 API")
public class ShopAuthController {

    private final AuthService authService;

    public ShopAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 會員註冊
     */
    @PostMapping("/register")
    @Operation(summary = "會員註冊", description = "註冊新會員帳號")
    public ResponseEntity<ApiResponse<MemberProfile>> register(@Valid @RequestBody RegisterRequest request) {
        Member member = authService.register(request.getEmail(), request.getPassword(), request.getName());

        MemberProfile profile = new MemberProfile(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getHomeAddress(),
                member.getEmailVerified(),
                member.getAvatarUrl(),
                member.getAvatarUpdatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success("註冊成功", profile));
    }

    /**
     * 會員登入
     */
    @PostMapping("/login")
    @Operation(summary = "會員登入", description = "使用 Email 和密碼登入")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.getEmail(), request.getPassword());

        MemberProfile profile = new MemberProfile(
                result.member.getId(),
                result.member.getEmail(),
                result.member.getName(),
                result.member.getPhone(),
                result.member.getHomeAddress(),
                result.member.getEmailVerified(),
                result.member.getAvatarUrl(),
                result.member.getAvatarUpdatedAt()
        );

        LoginResponse response = new LoginResponse(result.accessToken, result.refreshToken, profile);

        return ResponseEntity.ok(ApiResponse.success("登入成功", response));
    }

    /**
     * 會員登出
     * 撤銷所有 Refresh Token 並將當前 Access Token 加入黑名單
     */
    @PostMapping("/logout")
    @Operation(summary = "會員登出", description = "撤銷所有 Refresh Token 並將 Access Token 加入黑名單")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        // 從 Authorization Header 提取 Access Token
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        // 執行登出邏輯
        authService.logout(userDetails.getUserId(), accessToken);

        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    /**
     * 更新 Access Token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "更新 Access Token", description = "使用 Refresh Token 取得新的 Access Token")
    public ResponseEntity<ApiResponse<String>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthService.TokenRefreshResult result = authService.refreshAccessToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token 更新成功", result.accessToken));
    }

    /**
     * 驗證 Email
     */
    @PostMapping("/verify-email")
    @Operation(summary = "驗證 Email", description = "使用驗證 Token 驗證 Email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getToken());

        return ResponseEntity.ok(ApiResponse.success("Email 驗證成功", VerifyEmailResponse.success()));
    }

    /**
     * 重新發送驗證郵件
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "重新發送驗證郵件", description = "重新發送 Email 驗證郵件")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        authService.resendVerificationEmail(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success("驗證郵件已重新發送"));
    }

    /**
     * 忘記密碼
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘記密碼", description = "申請密碼重設，系統將發送重設郵件")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success("密碼重設郵件已發送", ForgotPasswordResponse.success()));
    }

    /**
     * 重設密碼
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重設密碼", description = "使用重設 Token 重設密碼")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());

        return ResponseEntity.ok(ApiResponse.success("密碼重設成功", ResetPasswordResponse.success()));
    }
}
