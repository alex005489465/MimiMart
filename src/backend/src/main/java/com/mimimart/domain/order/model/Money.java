package com.mimimart.domain.order.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金額值對象
 * 封裝金額運算邏輯與驗證規則
 */
public class Money {
    private static final int SCALE = 2; // 小數位數
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP; // 四捨五入

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 建立金額值對象
     *
     * @param amount 金額
     * @return Money 實例
     * @throws IllegalArgumentException 如果金額為負數
     */
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("金額不可為 null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金額不可為負數");
        }
        return new Money(amount);
    }

    /**
     * 建立金額值對象(從 double)
     */
    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    /**
     * 建立零元
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /**
     * 加法運算
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * 減法運算
     */
    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金額運算結果不可為負數");
        }
        return new Money(result);
    }

    /**
     * 乘法運算(用於計算小計)
     */
    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("數量不可為負數");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    /**
     * 比較大小
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 取得金額值
     */
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
