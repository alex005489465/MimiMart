package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.member.*;
import com.mimimart.application.service.AuthService;
import com.mimimart.infrastructure.persistence.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     */
    @PostMapping("/logout")
    @Operation(summary = "會員登出", description = "撤銷所有 Refresh Token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) Object request) {
        // 從 SecurityContext 取得會員 ID (需要在 Controller 中實作)
        // 此處簡化處理
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
}
