package com.mimimart.domain.cart.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 購物車已滿異常
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public class CartFullException extends DomainException {

    private final Integer maxItemsCount;

    public CartFullException(Integer maxItemsCount) {
        super(String.format("購物車已達上限，最多只能放入 %d 種商品", maxItemsCount));
        this.maxItemsCount = maxItemsCount;
    }

    public Integer getMaxItemsCount() {
        return maxItemsCount;
    }
}
