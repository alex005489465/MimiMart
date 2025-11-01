package com.mimimart.domain.payment.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 付款已完成異常
 * 當嘗試對已付款的記錄進行不允許的操作時拋出
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
public class PaymentAlreadyPaidException extends DomainException {

    public PaymentAlreadyPaidException(String paymentNumber) {
        super("付款已完成,無法執行此操作: " + paymentNumber);
    }
}
