package com.mimimart.domain.order.exception;

/**
 * 未授權的訂單存取異常
 */
public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(String orderNumber) {
        super("您無權存取此訂單: " + orderNumber);
    }
}
