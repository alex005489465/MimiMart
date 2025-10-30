package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * Email 已驗證異常
 */
public class EmailAlreadyVerifiedException extends DomainException {
    public EmailAlreadyVerifiedException(String message) {
        super("EMAIL_ALREADY_VERIFIED", message);
    }
}
