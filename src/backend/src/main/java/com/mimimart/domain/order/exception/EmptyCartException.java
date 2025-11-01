package com.mimimart.domain.order.exception;

/**
 * 購物車為空異常
 */
public class EmptyCartException extends RuntimeException {
    public EmptyCartException() {
        super("購物車為空,無法建立訂單");
    }
}
