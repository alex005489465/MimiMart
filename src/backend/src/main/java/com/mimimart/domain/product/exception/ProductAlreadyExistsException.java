package com.mimimart.domain.product.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 商品已存在異常
 */
public class ProductAlreadyExistsException extends DomainException {

    public ProductAlreadyExistsException(String productName) {
        super("PRODUCT_ALREADY_EXISTS", "商品名稱已存在: " + productName);
    }
}
