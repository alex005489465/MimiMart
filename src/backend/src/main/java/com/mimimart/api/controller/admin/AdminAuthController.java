package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.admin.AdminLoginRequest;
import com.mimimart.api.dto.admin.AdminLoginResponse;
import com.mimimart.api.dto.admin.AdminProfile;
import com.mimimart.api.dto.member.RefreshTokenRequest;
import com.mimimart.application.service.AdminService;
import com.mimimart.infrastructure.persistence.entity.Admin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 後台管理員認證 Controller
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "管理員認證", description = "管理員登入、登出相關 API")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 管理員登入
     */
    @PostMapping("/login")
    @Operation(summary = "管理員登入", description = "使用帳號或 Email 和密碼登入")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminService.AdminLoginResult result = adminService.login(request.getUsername(), request.getPassword());

        AdminProfile profile = new AdminProfile(
                result.admin.getId(),
                result.admin.getUsername(),
                result.admin.getEmail(),
                result.admin.getName()
        );

        AdminLoginResponse response = new AdminLoginResponse(result.accessToken, result.refreshToken, profile);

        return ResponseEntity.ok(ApiResponse.success("登入成功", response));
    }

    /**
     * 管理員登出
     */
    @PostMapping("/logout")
    @Operation(summary = "管理員登出", description = "撤銷所有 Refresh Token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) Object request) {
        // 從 SecurityContext 取得管理員 ID (需要在 Controller 中實作)
        // 此處簡化處理
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    /**
     * 更新 Access Token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "更新 Access Token", description = "使用 Refresh Token 取得新的 Access Token")
    public ResponseEntity<ApiResponse<String>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AdminService.AdminTokenRefreshResult result = adminService.refreshAccessToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token 更新成功", result.accessToken));
    }
}
