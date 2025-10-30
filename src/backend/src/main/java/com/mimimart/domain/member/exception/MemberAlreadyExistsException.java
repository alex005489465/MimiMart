package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 會員已存在異常
 */
public class MemberAlreadyExistsException extends DomainException {
    public MemberAlreadyExistsException(String message) {
        super("MEMBER_ALREADY_EXISTS", message);
    }
}
