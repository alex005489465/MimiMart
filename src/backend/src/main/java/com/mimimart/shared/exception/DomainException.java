package com.mimimart.shared.exception;

import lombok.Getter;

/**
 * 領域異常基礎類別
 * 所有業務領域的自定義異常都應繼承此類別
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
    }

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOMAIN_ERROR";
    }

    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
