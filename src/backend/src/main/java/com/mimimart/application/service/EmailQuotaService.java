package com.mimimart.application.service;

import com.mimimart.domain.email.exception.EmailQuotaExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * 郵件配額服務
 * 使用 Redis 管理月度發信總量配額
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQuotaService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailSendLogService emailSendLogService;

    @Value("${mimimart.email.quota.monthly-limit}")
    private int monthlyLimit;

    /**
     * Redis key 前綴：email:quota:monthly
     */
    private static final String QUOTA_KEY_PREFIX = "email:quota:monthly:";

    /**
     * 檢查並遞增月度發信配額
     * 如果超過限制則拋出例外
     *
     * @throws EmailQuotaExceededException 當超過月度配額時
     */
    public void checkAndIncrementQuota() {
        String currentMonth = getCurrentMonth();
        String key = QUOTA_KEY_PREFIX + currentMonth;

        // 先從 Redis 獲取當前計數
        String countStr = redisTemplate.opsForValue().get(key);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;

        // 如果 Redis 中沒有記錄，從資料庫同步
        if (countStr == null) {
            currentCount = syncQuotaFromDatabase(currentMonth);
        }

        // 檢查是否超過配額
        if (currentCount >= monthlyLimit) {
            log.warn("郵件配額已達上限：當月已發送 {} 封，上限 {} 封", currentCount, monthlyLimit);
            throw new EmailQuotaExceededException(
                    String.format("本月發信配額已達上限（%d/%d），請聯繫管理員", currentCount, monthlyLimit)
            );
        }

        // 遞增計數器
        Long newCount = redisTemplate.opsForValue().increment(key);

        // 設定過期時間（當月結束後 7 天）
        if (newCount != null && newCount == 1) {
            long ttlSeconds = calculateTtlSeconds();
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }

        log.debug("郵件配額已遞增：{} -> {}/{}", key, newCount, monthlyLimit);
    }

    /**
     * 獲取當前月度的發信使用量
     *
     * @return 當月已發送的郵件數量
     */
    public long getCurrentMonthUsage() {
        String currentMonth = getCurrentMonth();
        String key = QUOTA_KEY_PREFIX + currentMonth;

        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr != null) {
            return Long.parseLong(countStr);
        }

        // 如果 Redis 中沒有，從資料庫同步
        return syncQuotaFromDatabase(currentMonth);
    }

    /**
     * 獲取當前月度配額使用率
     *
     * @return 使用率（0-100）
     */
    public double getCurrentMonthUsagePercentage() {
        long usage = getCurrentMonthUsage();
        return (double) usage / monthlyLimit * 100;
    }

    /**
     * 從資料庫同步配額計數到 Redis
     *
     * @param yearMonth 年月字串（格式：YYYY-MM）
     * @return 從資料庫統計的發信數量
     */
    private long syncQuotaFromDatabase(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime startTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);

        long count = emailSendLogService.countEmailsSentBetween(startTime, endTime);

        // 同步到 Redis
        String key = QUOTA_KEY_PREFIX + yearMonth;
        redisTemplate.opsForValue().set(key, String.valueOf(count));

        // 設定過期時間
        long ttlSeconds = calculateTtlSeconds();
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);

        log.info("已從資料庫同步郵件配額：{} = {}", key, count);
        return count;
    }

    /**
     * 獲取當前年月字串
     *
     * @return 格式：YYYY-MM
     */
    private String getCurrentMonth() {
        return YearMonth.now().toString();
    }

    /**
     * 計算 TTL（到當月結束後 7 天）
     *
     * @return TTL 秒數
     */
    private long calculateTtlSeconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfMonth = YearMonth.from(now).atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime expiryTime = endOfMonth.plusDays(7);

        return java.time.Duration.between(now, expiryTime).getSeconds();
    }

    /**
     * 重置當月配額（僅用於測試或管理功能）
     */
    public void resetCurrentMonthQuota() {
        String currentMonth = getCurrentMonth();
        String key = QUOTA_KEY_PREFIX + currentMonth;
        redisTemplate.delete(key);
        log.warn("已重置當月郵件配額：{}", key);
    }
}
