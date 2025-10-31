package com.mimimart.domain.product.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效價格異常
 */
public class InvalidPriceException extends DomainException {

    public InvalidPriceException(String message) {
        super("INVALID_PRICE", message);
    }
}
