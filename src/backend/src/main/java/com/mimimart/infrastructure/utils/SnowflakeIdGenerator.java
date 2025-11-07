package com.mimimart.infrastructure.utils;

/**
 * 雪花算法 ID 生成器
 * 生成 64 位元全域唯一 ID
 *
 * 結構：1 bit (符號位) + 41 bits (時間戳) + 10 bits (工作機器 ID) + 12 bits (序列號)
 *
 * 特點：
 * - 趨勢遞增：基於時間戳，大致有序
 * - 高效能：單機每毫秒可生成 4096 個 ID
 * - 全域唯一：即使分散式部署也不會重複
 * - 無需資料庫：純記憶體計算
 */
public class SnowflakeIdGenerator {

    /**
     * 起始時間戳 (2025-01-01 00:00:00 UTC+8)
     */
    private static final long EPOCH = 1735660800000L;

    /**
     * 工作機器 ID 位數 (10 bits = 1024 台機器)
     */
    private static final long WORKER_ID_BITS = 10L;

    /**
     * 序列號位數 (12 bits = 每毫秒 4096 個 ID)
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 最大工作機器 ID (1023)
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 最大序列號 (4095)
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作機器 ID 左移位數 (12)
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 時間戳左移位數 (22)
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 工作機器 ID (0-1023)
     */
    private final long workerId;

    /**
     * 序列號 (0-4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成 ID 的時間戳
     */
    private long lastTimestamp = -1L;

    /**
     * 建構子
     *
     * @param workerId 工作機器 ID (0-1023)
     */
    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    String.format("Worker ID 必須在 0 到 %d 之間", MAX_WORKER_ID));
        }
        this.workerId = workerId;
    }

    /**
     * 生成下一個 ID（執行緒安全）
     *
     * @return 64 位元全域唯一 ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // 時鐘回撥檢查
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("時鐘回撥錯誤：拒絕生成 ID %d 毫秒", lastTimestamp - timestamp));
        }

        // 同一毫秒內
        if (timestamp == lastTimestamp) {
            // 序列號自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列號溢位，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒，序列號重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 組合 ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 獲取當前時間戳（毫秒）
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 解析 ID 的組成部分（用於除錯）
     *
     * @param id 雪花 ID
     * @return 包含時間戳、工作機器 ID、序列號的資訊
     */
    public static String parse(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long sequence = id & MAX_SEQUENCE;
        return String.format("時間戳=%d, 工作機器ID=%d, 序列號=%d", timestamp, workerId, sequence);
    }
}
