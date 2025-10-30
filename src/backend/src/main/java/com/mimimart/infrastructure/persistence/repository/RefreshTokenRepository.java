package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.RefreshToken;
import com.mimimart.shared.valueobject.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 根據 Token 查詢
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 刪除用戶的所有 Token (登出)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.memberId = :userId AND rt.userType = :userType")
    void deleteByMemberIdAndUserType(Long userId, UserType userType);

    /**
     * 刪除過期的 Token
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
