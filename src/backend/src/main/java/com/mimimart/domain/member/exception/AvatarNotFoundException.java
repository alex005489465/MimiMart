package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 頭貼不存在異常
 * 當查詢的頭貼不存在時拋出
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public class AvatarNotFoundException extends DomainException {
    public AvatarNotFoundException(String message) {
        super(message);
    }
}
