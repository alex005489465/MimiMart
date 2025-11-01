package com.mimimart.domain.payment.exception;

import com.mimimart.shared.exception.DomainException;

import java.math.BigDecimal;

/**
 * 付款金額不符異常
 * 當回調金額與原始金額不符時拋出
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
public class PaymentAmountMismatchException extends DomainException {

    public PaymentAmountMismatchException(BigDecimal expected, BigDecimal actual) {
        super(String.format("付款金額不符: 預期 %s,實際 %s", expected, actual));
    }
}
