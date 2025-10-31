package com.mimimart.domain.member.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效的頭貼檔案異常
 * 當上傳的頭貼檔案格式或大小不符合要求時拋出
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public class InvalidAvatarException extends DomainException {
    public InvalidAvatarException(String message) {
        super(message);
    }
}
