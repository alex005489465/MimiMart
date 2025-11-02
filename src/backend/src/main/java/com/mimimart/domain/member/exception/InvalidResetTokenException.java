package com.mimimart.domain.member.exception;

/**
 * 無效的密碼重設 Token 例外
 */
public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException(String message) {
        super(message);
    }

    public InvalidResetTokenException() {
        super("無效的密碼重設 Token");
    }
}
