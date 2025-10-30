package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效憑證異常
 */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message);
    }
}
