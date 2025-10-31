package com.mimimart.domain.product.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 商品不存在異常
 */
public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(String message) {
        super("PRODUCT_NOT_FOUND", message);
    }

    public ProductNotFoundException(Long productId) {
        super("PRODUCT_NOT_FOUND", "商品不存在: ID=" + productId);
    }
}
