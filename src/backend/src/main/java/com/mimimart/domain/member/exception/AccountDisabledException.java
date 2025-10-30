package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 帳號停用異常
 */
public class AccountDisabledException extends DomainException {
    public AccountDisabledException(String message) {
        super("ACCOUNT_DISABLED", message);
    }
}
