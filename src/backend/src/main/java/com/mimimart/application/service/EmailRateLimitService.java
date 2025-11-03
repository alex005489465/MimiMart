package com.mimimart.application.service;

import com.mimimart.domain.email.exception.EmailRateLimitExceededException;
import com.mimimart.shared.valueobject.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 郵件頻率限制服務
 * 使用 Redis 管理會員的發信頻率限制
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${mimimart.email.rate-limit.verification.max-attempts}")
    private int verificationMaxAttempts;

    @Value("${mimimart.email.rate-limit.verification.window-minutes}")
    private int verificationWindowMinutes;

    @Value("${mimimart.email.rate-limit.password-reset.max-attempts}")
    private int passwordResetMaxAttempts;

    @Value("${mimimart.email.rate-limit.password-reset.window-minutes}")
    private int passwordResetWindowMinutes;

    /**
     * Redis key 前綴：email:ratelimit:member
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "email:ratelimit:member:";

    /**
     * 檢查並記錄會員發信行為
     * 如果超過頻率限制則拋出例外
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     * @throws EmailRateLimitExceededException 當超過頻率限制時
     */
    public void checkAndRecordRateLimit(Long memberId, EmailType emailType) {
        String key = buildKey(memberId, emailType);
        int maxAttempts = getMaxAttempts(emailType);
        int windowMinutes = getWindowMinutes(emailType);

        // 獲取當前計數
        String countStr = redisTemplate.opsForValue().get(key);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;

        // 檢查是否超過限制
        if (currentCount >= maxAttempts) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
            log.warn("會員 {} 發送 {} 郵件超過頻率限制：{}/{} 次",
                    memberId, emailType, currentCount, maxAttempts);
            throw new EmailRateLimitExceededException(
                    String.format("發送郵件過於頻繁，請在 %d 分鐘後再試", ttl != null ? ttl : windowMinutes)
            );
        }

        // 遞增計數器
        Long newCount = redisTemplate.opsForValue().increment(key);

        // 首次設定時，設定過期時間
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(key, windowMinutes, TimeUnit.MINUTES);
        }

        log.debug("會員 {} 發送 {} 郵件頻率記錄：{}/{} 次",
                memberId, emailType, newCount, maxAttempts);
    }

    /**
     * 檢查會員是否可以發送該類型郵件
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     * @return true 如果可以發送
     */
    public boolean canSendEmail(Long memberId, EmailType emailType) {
        String key = buildKey(memberId, emailType);
        int maxAttempts = getMaxAttempts(emailType);

        String countStr = redisTemplate.opsForValue().get(key);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;

        return currentCount < maxAttempts;
    }

    /**
     * 獲取會員剩餘可發送次數
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     * @return 剩餘次數
     */
    public int getRemainingAttempts(Long memberId, EmailType emailType) {
        String key = buildKey(memberId, emailType);
        int maxAttempts = getMaxAttempts(emailType);

        String countStr = redisTemplate.opsForValue().get(key);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;

        return Math.max(0, maxAttempts - (int) currentCount);
    }

    /**
     * 重置會員的頻率限制（僅用於測試或特殊情況）
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     */
    public void resetRateLimit(Long memberId, EmailType emailType) {
        String key = buildKey(memberId, emailType);
        redisTemplate.delete(key);
        log.info("已重置會員 {} 的 {} 郵件頻率限制", memberId, emailType);
    }

    /**
     * 建立 Redis key
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     * @return Redis key
     */
    private String buildKey(Long memberId, EmailType emailType) {
        return RATE_LIMIT_KEY_PREFIX + memberId + ":" + emailType.name();
    }

    /**
     * 根據郵件類型獲取最大嘗試次數
     *
     * @param emailType 郵件類型
     * @return 最大嘗試次數
     */
    private int getMaxAttempts(EmailType emailType) {
        return switch (emailType) {
            case VERIFICATION -> verificationMaxAttempts;
            case PASSWORD_RESET -> passwordResetMaxAttempts;
            default -> 3; // 預設值
        };
    }

    /**
     * 根據郵件類型獲取時間窗口（分鐘）
     *
     * @param emailType 郵件類型
     * @return 時間窗口（分鐘）
     */
    private int getWindowMinutes(EmailType emailType) {
        return switch (emailType) {
            case VERIFICATION -> verificationWindowMinutes;
            case PASSWORD_RESET -> passwordResetWindowMinutes;
            default -> 10; // 預設值
        };
    }
}
