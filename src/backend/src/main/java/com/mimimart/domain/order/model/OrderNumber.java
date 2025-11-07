package com.mimimart.domain.order.model;

import com.mimimart.infrastructure.utils.SnowflakeIdGenerator;

import java.util.Objects;

/**
 * 訂單編號值對象
 * 格式：ORD + 19 位雪花算法 ID
 * 範例：ORD1234567890123456789
 *
 * 特點：
 * - 全域唯一：即使分散式部署也不會重複
 * - 趨勢遞增：基於時間戳，大致有序
 * - 高效能：純記憶體計算，無需查詢資料庫
 */
public class OrderNumber {
    private static final String PREFIX = "ORD";

    /**
     * 雪花算法生成器（工作機器 ID = 0）
     * 注意：若部署多台機器，需為每台機器配置不同的 workerId
     */
    private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator(0);

    private final String value;

    private OrderNumber(String value) {
        this.value = value;
    }

    /**
     * 生成新的訂單編號
     */
    public static OrderNumber generate() {
        long snowflakeId = ID_GENERATOR.nextId();
        return new OrderNumber(PREFIX + snowflakeId);
    }

    /**
     * 從現有字串建立訂單編號(用於查詢)
     */
    public static OrderNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("訂單編號不可為空");
        }
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("訂單編號格式錯誤:必須以 " + PREFIX + " 開頭");
        }
        return new OrderNumber(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderNumber that = (OrderNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
