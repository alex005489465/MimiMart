package com.mimimart.domain.shipment.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流資訊值對象
 * 由後台管理員在出貨時填寫
 */
@Getter
@Builder
public class ShippingInfo {
    /**
     * 物流商名稱（例如：黑貓、新竹、順豐等）
     */
    private final String carrier;

    /**
     * 物流追蹤號碼
     */
    private final String trackingNumber;

    /**
     * 實際出貨時間
     */
    private final LocalDateTime shippedAt;

    /**
     * 預計送達日期
     */
    private final LocalDate estimatedDeliveryDate;

    /**
     * 驗證物流資訊
     */
    public void validate() {
        if (carrier == null || carrier.trim().isEmpty()) {
            throw new IllegalArgumentException("物流商名稱不可為空");
        }
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("物流追蹤號碼不可為空");
        }
        if (shippedAt == null) {
            throw new IllegalArgumentException("出貨時間不可為空");
        }
    }
}
