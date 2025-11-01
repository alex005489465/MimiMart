package com.mimimart.domain.payment.exception;

import com.mimimart.infrastructure.persistence.entity.PaymentStatus;
import com.mimimart.shared.exception.DomainException;

/**
 * 無效的付款狀態異常
 * 當付款狀態不允許執行某操作時拋出
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
public class InvalidPaymentStatusException extends DomainException {

    public InvalidPaymentStatusException(String operation, PaymentStatus currentStatus) {
        super(String.format("無法%s:當前付款狀態為 %s", operation, currentStatus));
    }
}
