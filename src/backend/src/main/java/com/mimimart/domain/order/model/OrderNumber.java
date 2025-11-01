package com.mimimart.domain.order.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

/**
 * 訂單編號值對象
 * 格式:ORD + yyyyMMddHHmmss + 3位隨機數
 * 範例:ORD20250131143025123
 */
public class OrderNumber {
    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    private final String value;

    private OrderNumber(String value) {
        this.value = value;
    }

    /**
     * 生成新的訂單編號
     */
    public static OrderNumber generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String randomPart = String.format("%03d", RANDOM.nextInt(1000));
        return new OrderNumber(PREFIX + timestamp + randomPart);
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
