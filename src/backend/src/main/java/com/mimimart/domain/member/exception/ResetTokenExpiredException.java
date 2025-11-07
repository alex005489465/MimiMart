package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 密碼重設 Token 已過期例外
 */
public class ResetTokenExpiredException extends DomainException {

    public ResetTokenExpiredException(String message) {
        super("RESET_TOKEN_EXPIRED", message);
    }

    public ResetTokenExpiredException() {
        super("RESET_TOKEN_EXPIRED", "密碼重設 Token 已過期，請重新申請");
    }
}
