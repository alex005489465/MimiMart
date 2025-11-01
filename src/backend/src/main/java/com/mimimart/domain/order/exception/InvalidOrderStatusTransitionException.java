package com.mimimart.domain.order.exception;

/**
 * 無效的訂單狀態轉換異常
 */
public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
