package com.mimimart.infrastructure.security;

import com.mimimart.shared.valueobject.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具類
 * 負責簽發、驗證和解析 JSON Web Token
 * 支援前台會員與後台管理員的雙用戶類型
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.member-access-token-expiration}")
    private Long memberAccessTokenExpiration;

    @Value("${jwt.admin-access-token-expiration}")
    private Long adminAccessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    /**
     * 產生前台會員 Access Token
     * 包含會員 ID、Email 和角色資訊
     *
     * @param memberId 會員 ID
     * @param email 電子郵件
     * @return Access Token
     */
    public String generateAccessToken(Long memberId, String email) {
        return generateAccessToken(memberId, email, UserType.MEMBER);
    }

    /**
     * 產生後台管理員 Access Token
     * 包含管理員 ID、Email 和角色資訊，有效期較長 (30 分鐘)
     *
     * @param adminId 管理員 ID
     * @param email 管理員 Email
     * @return Access Token
     */
    public String generateAdminAccessToken(Long adminId, String email) {
        return generateAccessToken(adminId, email, UserType.ADMIN);
    }

    /**
     * 產生 Access Token (通用方法)
     *
     * @param userId 用戶 ID
     * @param email Email
     * @param userType 用戶類型
     * @return Access Token
     */
    private String generateAccessToken(Long userId, String email, UserType userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("userType", userType.name());
        claims.put("role", userType == UserType.ADMIN ? "ADMIN" : "MEMBER");

        long expiration = userType == UserType.ADMIN ? adminAccessTokenExpiration : memberAccessTokenExpiration;

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 產生 Refresh Token
     * 僅包含用戶 ID (最小資訊原則)
     *
     * @param userId 用戶 ID
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 驗證 Token 有效性
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 從 Token 提取 Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 從 Token 提取 Email
     *
     * @param token JWT Token
     * @return 電子郵件
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 從 Token 提取用戶 ID
     *
     * @param token JWT Token
     * @return 用戶 ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            throw new IllegalArgumentException("Invalid userId in token");
        }
    }

    /**
     * 從 Token 提取用戶類型
     *
     * @param token JWT Token
     * @return 用戶類型
     */
    public UserType extractUserType(String token) {
        Claims claims = extractClaims(token);
        String userType = (String) claims.get("userType");

        if (userType == null) {
            // 預設為 MEMBER
            return UserType.MEMBER;
        }

        return UserType.valueOf(userType);
    }

    /**
     * 檢查 Token 是否過期
     *
     * @param token JWT Token
     * @return 是否過期
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * 取得簽名金鑰
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
