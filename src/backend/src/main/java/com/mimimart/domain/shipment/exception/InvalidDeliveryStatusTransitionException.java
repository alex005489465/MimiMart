package com.mimimart.domain.shipment.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效的配送狀態轉換異常
 */
public class InvalidDeliveryStatusTransitionException extends DomainException {
    public InvalidDeliveryStatusTransitionException(String message) {
        super(message);
    }
}
