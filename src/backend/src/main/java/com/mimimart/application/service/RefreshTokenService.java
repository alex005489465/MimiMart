package com.mimimart.application.service;

import com.mimimart.infrastructure.persistence.entity.RefreshToken;
import com.mimimart.infrastructure.persistence.repository.RefreshTokenRepository;
import com.mimimart.shared.valueobject.UserType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Refresh Token 服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 儲存 Refresh Token
     * 採用單裝置登入策略：每次儲存新 Token 前，先刪除該用戶所有舊 Token
     */
    @Transactional
    public void saveRefreshToken(Long userId, String token, UserType userType, LocalDateTime expiresAt) {
        // 先刪除該用戶所有舊 Token（單裝置登入策略）
        refreshTokenRepository.deleteByMemberIdAndUserType(userId, userType);

        // 再新增新 Token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setMemberId(userId);
        refreshToken.setToken(token);
        refreshToken.setUserType(userType);
        refreshToken.setExpiresAt(expiresAt);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 驗證 Refresh Token 是否有效
     */
    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /**
     * 根據 Token 取得用戶 ID
     */
    public Long getUserIdByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getMemberId)
                .orElse(null);
    }

    /**
     * 根據 Token 取得用戶類型
     */
    public UserType getUserTypeByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUserType)
                .orElse(null);
    }

    /**
     * 撤銷用戶的所有 Refresh Token (登出)
     */
    @Transactional
    public void revokeAllTokens(Long userId, UserType userType) {
        refreshTokenRepository.deleteByMemberIdAndUserType(userId, userType);
    }

    /**
     * 刪除過期的 Token (定時任務)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
