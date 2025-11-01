package com.mimimart.domain.payment.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

/**
 * 付款編號值對象 (Value Object)
 * 封裝付款編號的生成與驗證邏輯
 *
 * 設計理念:
 * - 不可變對象
 * - 封裝生成規則 (PAY + yyyyMMddHHmmss + 隨機3碼)
 * - 型別安全,避免將 String 誤用為付款編號
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
public class PaymentNumber {

    private static final String PREFIX = "PAY";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    private final String value;

    /**
     * 私有建構函式,防止外部直接實例化
     */
    private PaymentNumber(String value) {
        Objects.requireNonNull(value, "付款編號不能為 null");
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("付款編號格式錯誤: " + value);
        }
        this.value = value;
    }

    /**
     * 從現有編號建立值對象 (用於從資料庫載入)
     *
     * @param value 付款編號
     * @return PaymentNumber 值對象
     */
    public static PaymentNumber of(String value) {
        return new PaymentNumber(value);
    }

    /**
     * 生成新的付款編號
     * 格式: PAY + yyyyMMddHHmmss + 隨機3碼
     *
     * @return 新的 PaymentNumber 值對象
     */
    public static PaymentNumber generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int randomNum = RANDOM.nextInt(1000);
        String value = String.format("%s%s%03d", PREFIX, timestamp, randomNum);
        return new PaymentNumber(value);
    }

    /**
     * 取得付款編號字串
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentNumber that = (PaymentNumber) o;
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
