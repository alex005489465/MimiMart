package com.mimimart.domain.member.exception;

/**
 * 密碼不一致例外
 */
public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException(String message) {
        super(message);
    }

    public PasswordMismatchException() {
        super("新密碼與確認密碼不一致");
    }
}
