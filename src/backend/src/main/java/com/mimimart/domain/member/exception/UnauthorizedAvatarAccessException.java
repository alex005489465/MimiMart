package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 未授權的頭貼訪問異常
 * 當嘗試訪問非自己的頭貼時拋出
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public class UnauthorizedAvatarAccessException extends DomainException {
    public UnauthorizedAvatarAccessException(String message) {
        super(message);
    }
}
