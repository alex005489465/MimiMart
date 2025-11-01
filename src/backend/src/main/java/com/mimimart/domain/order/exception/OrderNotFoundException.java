package com.mimimart.domain.order.exception;

/**
 * 訂單不存在異常
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderNumber) {
        super("找不到訂單: " + orderNumber);
    }
}
