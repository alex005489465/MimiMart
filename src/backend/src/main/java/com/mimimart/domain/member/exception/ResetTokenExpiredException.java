package com.mimimart.domain.member.exception;

/**
 * 密碼重設 Token 已過期例外
 */
public class ResetTokenExpiredException extends RuntimeException {

    public ResetTokenExpiredException(String message) {
        super(message);
    }

    public ResetTokenExpiredException() {
        super("密碼重設 Token 已過期，請重新申請");
    }
}
