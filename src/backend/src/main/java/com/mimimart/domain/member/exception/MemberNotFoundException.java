package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 會員不存在異常
 */
public class MemberNotFoundException extends DomainException {
    public MemberNotFoundException(String message) {
        super("MEMBER_NOT_FOUND", message);
    }
}
