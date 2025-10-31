package com.mimimart.domain.cart.exception;

/**
 * 購物車項目不存在異常
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(String message) {
        super(message);
    }

    public CartItemNotFoundException(Long productId) {
        super("購物車中未找到商品 ID: " + productId);
    }
}
