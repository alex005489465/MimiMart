package com.mimimart.domain.payment.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 付款記錄不存在異常
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
public class PaymentNotFoundException extends DomainException {

    public PaymentNotFoundException(String paymentNumber) {
        super("付款記錄不存在: " + paymentNumber);
    }

    public PaymentNotFoundException(Long orderId) {
        super("訂單 " + orderId + " 的付款記錄不存在");
    }
}
