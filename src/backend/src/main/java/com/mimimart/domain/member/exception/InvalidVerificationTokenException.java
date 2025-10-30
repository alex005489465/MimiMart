package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效驗證 Token 異常
 */
public class InvalidVerificationTokenException extends DomainException {
    public InvalidVerificationTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
}
