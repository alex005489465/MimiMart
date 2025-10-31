package com.mimimart.shared.valueobject;

import com.mimimart.domain.product.exception.InvalidPriceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 價格值對象 (不可變)
 * 封裝價格邏輯和驗證
 */
public class Price {

    private final BigDecimal price;
    private final BigDecimal originalPrice;

    private Price(BigDecimal price, BigDecimal originalPrice) {
        validatePrice(price, originalPrice);
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.originalPrice = originalPrice != null ? originalPrice.setScale(2, RoundingMode.HALF_UP) : null;
    }

    /**
     * 建立價格 (僅售價)
     */
    public static Price of(BigDecimal price) {
        return new Price(price, null);
    }

    /**
     * 建立價格 (含原價)
     */
    public static Price of(BigDecimal price, BigDecimal originalPrice) {
        return new Price(price, originalPrice);
    }

    /**
     * 驗證價格
     */
    private void validatePrice(BigDecimal price, BigDecimal originalPrice) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException("價格必須大於 0");
        }

        if (originalPrice != null) {
            if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidPriceException("原價必須大於 0");
            }
            if (originalPrice.compareTo(price) < 0) {
                throw new InvalidPriceException("原價不能低於售價");
            }
        }
    }

    /**
     * 取得售價
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * 取得原價
     */
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    /**
     * 檢查是否有折扣
     */
    public boolean hasDiscount() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
    }

    /**
     * 計算折扣百分比 (例如: 0.20 表示 20% off)
     */
    public BigDecimal getDiscountPercentage() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = originalPrice.subtract(price);
        return discount.divide(originalPrice, 4, RoundingMode.HALF_UP);
    }

    /**
     * 計算折扣金額
     */
    public BigDecimal getDiscountAmount() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }

        return originalPrice.subtract(price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price1 = (Price) o;
        return Objects.equals(price, price1.price) && Objects.equals(originalPrice, price1.originalPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, originalPrice);
    }

    @Override
    public String toString() {
        if (hasDiscount()) {
            return String.format("Price{price=%s, originalPrice=%s, discount=%.0f%%}",
                price, originalPrice, getDiscountPercentage().multiply(BigDecimal.valueOf(100)));
        }
        return String.format("Price{price=%s}", price);
    }
}
