package com.mimimart.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名單服務
 * 使用 Redis 管理已登出的 Access Token 黑名單
 * 當用戶登出時，將 Token 加入黑名單，防止該 Token 被繼續使用
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Redis key 前綴：auth:blacklist:token
     */
    private static final String BLACKLIST_KEY_PREFIX = "auth:blacklist:token:";

    /**
     * 將 Token 加入黑名單
     * 使用 Token 的過期時間作為 Redis TTL，過期後自動清除
     *
     * @param token     JWT Token
     * @param expiresAt Token 過期時間
     */
    public void addToBlacklist(String token, Date expiresAt) {
        String key = buildKey(token);
        String value = LocalDateTime.now().toString(); // 儲存登出時間戳記

        // 計算 TTL（秒數）
        long ttlSeconds = calculateTTL(expiresAt);

        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            log.info("Token 已加入黑名單，TTL: {} 秒", ttlSeconds);
        } else {
            log.warn("Token 已過期，無需加入黑名單");
        }
    }

    /**
     * 檢查 Token 是否在黑名單中
     *
     * @param token JWT Token
     * @return true 如果 Token 在黑名單中
     */
    public boolean isBlacklisted(String token) {
        String key = buildKey(token);
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * 從黑名單中移除 Token（僅用於測試或特殊情況）
     *
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        String key = buildKey(token);
        redisTemplate.delete(key);
        log.info("Token 已從黑名單中移除");
    }

    /**
     * 建立 Redis key
     * 使用 SHA-256 雜湊 Token 以節省空間和保護隱私
     *
     * @param token JWT Token
     * @return Redis key
     */
    private String buildKey(String token) {
        String tokenHash = hashToken(token);
        return BLACKLIST_KEY_PREFIX + tokenHash;
    }

    /**
     * 使用 SHA-256 雜湊 Token
     * 避免儲存完整 Token，提升安全性
     *
     * @param token JWT Token
     * @return Token 雜湊值（十六進位字串）
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());

            // 轉換為十六進位字串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 演算法不存在", e);
            throw new RuntimeException("無法雜湊 Token", e);
        }
    }

    /**
     * 計算 Token 剩餘有效時間（秒數）
     *
     * @param expiresAt Token 過期時間
     * @return TTL 秒數
     */
    private long calculateTTL(Date expiresAt) {
        Instant now = Instant.now();
        Instant expiry = expiresAt.toInstant();
        long ttlSeconds = expiry.getEpochSecond() - now.getEpochSecond();
        return Math.max(0, ttlSeconds);
    }
}
