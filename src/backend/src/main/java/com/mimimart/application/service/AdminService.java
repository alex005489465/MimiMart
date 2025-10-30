package com.mimimart.application.service;

import com.mimimart.domain.member.exception.AccountDisabledException;
import com.mimimart.domain.member.exception.InvalidCredentialsException;
import com.mimimart.infrastructure.persistence.entity.Admin;
import com.mimimart.infrastructure.persistence.repository.AdminRepository;
import com.mimimart.infrastructure.security.JwtUtil;
import com.mimimart.shared.valueobject.AdminStatus;
import com.mimimart.shared.valueobject.UserType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理員服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AdminService(AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * 管理員登入
     */
    @Transactional
    public AdminLoginResult login(String usernameOrEmail, String password) {
        // 嘗試用 username 或 email 查詢
        Admin admin = adminRepository.findByUsername(usernameOrEmail)
                .or(() -> adminRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new InvalidCredentialsException("帳號或密碼錯誤"));

        // 驗證密碼
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new InvalidCredentialsException("帳號或密碼錯誤");
        }

        // 檢查帳號狀態
        if (admin.getStatus() != AdminStatus.ACTIVE) {
            throw new AccountDisabledException("帳號已被停用或封禁");
        }

        // 更新最後登入時間
        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);

        // 生成 Access Token 和 Refresh Token
        String accessToken = jwtUtil.generateAdminAccessToken(admin.getId(), admin.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId());

        // 儲存 Refresh Token
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(7);
        refreshTokenService.saveRefreshToken(admin.getId(), refreshToken, UserType.ADMIN, refreshTokenExpiresAt);

        return new AdminLoginResult(accessToken, refreshToken, admin);
    }

    /**
     * 管理員登出
     */
    @Transactional
    public void logout(Long adminId) {
        refreshTokenService.revokeAllTokens(adminId, UserType.ADMIN);
    }

    /**
     * 更新 Access Token
     */
    @Transactional
    public AdminTokenRefreshResult refreshAccessToken(String refreshToken) {
        // 驗證 Refresh Token
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Refresh Token 無效或已過期");
        }

        Long adminId = refreshTokenService.getUserIdByToken(refreshToken);
        UserType userType = refreshTokenService.getUserTypeByToken(refreshToken);

        if (adminId == null || userType != UserType.ADMIN) {
            throw new InvalidCredentialsException("Refresh Token 無效");
        }

        // 查詢管理員
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new InvalidCredentialsException("管理員不存在"));

        // 檢查帳號狀態
        if (admin.getStatus() != AdminStatus.ACTIVE) {
            throw new AccountDisabledException("帳號已被停用或封禁");
        }

        // 生成新的 Access Token
        String newAccessToken = jwtUtil.generateAdminAccessToken(admin.getId(), admin.getEmail());

        return new AdminTokenRefreshResult(newAccessToken);
    }

    /**
     * 管理員登入結果
     */
    public static class AdminLoginResult {
        public final String accessToken;
        public final String refreshToken;
        public final Admin admin;

        public AdminLoginResult(String accessToken, String refreshToken, Admin admin) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.admin = admin;
        }
    }

    /**
     * Token 更新結果
     */
    public static class AdminTokenRefreshResult {
        public final String accessToken;

        public AdminTokenRefreshResult(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
