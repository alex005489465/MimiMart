package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 密碼不一致例外
 */
public class PasswordMismatchException extends DomainException {

    public PasswordMismatchException(String message) {
        super("PASSWORD_MISMATCH", message);
    }

    public PasswordMismatchException() {
        super("PASSWORD_MISMATCH", "新密碼與確認密碼不一致");
    }
}
