package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 地址不存在異常
 */
public class AddressNotFoundException extends DomainException {
    public AddressNotFoundException(String message) {
        super("ADDRESS_NOT_FOUND", message);
    }
}
