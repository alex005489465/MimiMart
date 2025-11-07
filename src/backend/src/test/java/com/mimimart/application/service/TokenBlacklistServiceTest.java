package com.mimimart.application.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenBlacklistService 單元測試
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class TokenBlacklistServiceTest {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

    @BeforeEach
    void setUp() {
        // 清理測試資料
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理測試資料
        cleanupTestData();
    }

    private void cleanupTestData() {
        Set<String> keys = redisTemplate.keys("auth:blacklist:token:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void testAddToBlacklist_Success() {
        // Given: 準備一個有效期 1 小時的 Token
        Date expiresAt = new Date(System.currentTimeMillis() + 3600 * 1000);

        // When: 將 Token 加入黑名單
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expiresAt);

        // Then: Token 應該在黑名單中
        assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
    }

    @Test
    void testAddToBlacklist_ExpiredToken() {
        // Given: 準備一個已過期的 Token
        Date expiresAt = new Date(System.currentTimeMillis() - 1000);

        // When: 嘗試將過期 Token 加入黑名單
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expiresAt);

        // Then: Token 不應該在黑名單中（因為已過期）
        assertFalse(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
    }

    @Test
    void testIsBlacklisted_NotInBlacklist() {
        // Given: 一個未加入黑名單的 Token
        String notBlacklistedToken = "not.blacklisted.token";

        // When & Then: 檢查結果應為 false
        assertFalse(tokenBlacklistService.isBlacklisted(notBlacklistedToken));
    }

    @Test
    void testIsBlacklisted_InBlacklist() {
        // Given: 將 Token 加入黑名單
        Date expiresAt = new Date(System.currentTimeMillis() + 3600 * 1000);
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expiresAt);

        // When & Then: 檢查結果應為 true
        assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
    }

    @Test
    void testRemoveFromBlacklist() {
        // Given: 將 Token 加入黑名單
        Date expiresAt = new Date(System.currentTimeMillis() + 3600 * 1000);
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expiresAt);
        assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN));

        // When: 從黑名單中移除
        tokenBlacklistService.removeFromBlacklist(TEST_TOKEN);

        // Then: Token 不應該在黑名單中
        assertFalse(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
    }

    @Test
    void testMultipleTokens() {
        // Given: 準備多個 Token
        String token1 = "token.1";
        String token2 = "token.2";
        String token3 = "token.3";
        Date expiresAt = new Date(System.currentTimeMillis() + 3600 * 1000);

        // When: 將多個 Token 加入黑名單
        tokenBlacklistService.addToBlacklist(token1, expiresAt);
        tokenBlacklistService.addToBlacklist(token2, expiresAt);

        // Then: 只有加入的 Token 在黑名單中
        assertTrue(tokenBlacklistService.isBlacklisted(token1));
        assertTrue(tokenBlacklistService.isBlacklisted(token2));
        assertFalse(tokenBlacklistService.isBlacklisted(token3));
    }

    @Test
    void testTokenHashConsistency() {
        // Given: 同一個 Token
        Date expiresAt = new Date(System.currentTimeMillis() + 3600 * 1000);

        // When: 將 Token 加入黑名單
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expiresAt);

        // Then: 使用相同 Token 檢查應該一致
        assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
        assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
    }

    @Test
    void testShortTTL() throws InterruptedException {
        // Given: 準備一個 2 秒後過期的 Token
        Date expiresAt = new Date(System.currentTimeMillis() + 2000);

        // When: 將 Token 加入黑名單
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expiresAt);
        assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN));

        // Then: 等待 3 秒後，Token 應該自動從黑名單中移除
        Thread.sleep(3000);
        assertFalse(tokenBlacklistService.isBlacklisted(TEST_TOKEN));
    }
}
