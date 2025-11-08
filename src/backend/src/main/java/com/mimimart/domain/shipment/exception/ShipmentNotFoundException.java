package com.mimimart.domain.shipment.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 物流不存在異常
 */
public class ShipmentNotFoundException extends DomainException {
    public ShipmentNotFoundException(String message) {
        super(message);
    }

    public ShipmentNotFoundException(Long orderId) {
        super("找不到訂單 ID 為 " + orderId + " 的物流資訊");
    }
}
