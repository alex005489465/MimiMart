package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效的密碼重設 Token 例外
 */
public class InvalidResetTokenException extends DomainException {

    public InvalidResetTokenException(String message) {
        super("INVALID_RESET_TOKEN", message);
    }

    public InvalidResetTokenException() {
        super("INVALID_RESET_TOKEN", "無效的密碼重設 Token");
    }
}
