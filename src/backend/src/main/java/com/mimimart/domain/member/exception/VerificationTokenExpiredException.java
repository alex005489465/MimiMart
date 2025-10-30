package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 驗證 Token 過期異常
 */
public class VerificationTokenExpiredException extends DomainException {
    public VerificationTokenExpiredException(String message) {
        super("TOKEN_EXPIRED", message);
    }
}
