package com.mimimart.shared.valueobject;

import com.mimimart.domain.product.exception.InvalidPriceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 價格值物件 (不可變)
 * 封裝商品售價的邏輯與驗證
 */
public class Price {

    private final BigDecimal price;

    private Price(BigDecimal price) {
        validatePrice(price);
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 建立價格值物件
     *
     * @param price 商品售價,必須大於 0,範圍: 0.01 ~ 99,999,999.99
     * @return Price 值物件
     * @throws InvalidPriceException 當價格不符合規則時
     */
    public static Price of(BigDecimal price) {
        return new Price(price);
    }

    /**
     * 驗證價格
     */
    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException("商品售價必須大於 0");
        }

        if (price.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new InvalidPriceException("商品售價不能超過 99,999,999.99");
        }
    }

    /**
     * 取得商品售價
     */
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price priceObj = (Price) o;
        return Objects.equals(price, priceObj.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price);
    }

    @Override
    public String toString() {
        return String.format("Price{price=%s}", price);
    }
}
