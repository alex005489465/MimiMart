package com.mimimart.domain.payment.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 未授權存取付款記錄異常
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (Payment Security Enhancement)
 */
public class UnauthorizedPaymentAccessException extends DomainException {

    private static final String ERROR_CODE = "UNAUTHORIZED_PAYMENT_ACCESS";

    public UnauthorizedPaymentAccessException(String paymentNumber) {
        super(ERROR_CODE, String.format("無權存取付款記錄: %s", paymentNumber));
    }
}
