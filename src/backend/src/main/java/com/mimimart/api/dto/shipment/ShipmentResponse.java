package com.mimimart.api.dto.shipment;

import com.mimimart.domain.shipment.model.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流資訊回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物流資訊回應")
public class ShipmentResponse {
    @Schema(description = "物流 ID")
    private Long id;

    @Schema(description = "訂單 ID")
    private Long orderId;

    // 會員填寫的配送資訊
    @Schema(description = "收件人姓名")
    private String receiverName;

    @Schema(description = "收件人電話")
    private String receiverPhone;

    @Schema(description = "配送地址")
    private String shippingAddress;

    @Schema(description = "配送方式")
    private String deliveryMethod;

    @Schema(description = "配送備註")
    private String deliveryNote;

    @Schema(description = "運費")
    private BigDecimal shippingFee;

    // 管理員填寫的物流資訊
    @Schema(description = "物流商名稱")
    private String carrier;

    @Schema(description = "物流追蹤號碼")
    private String trackingNumber;

    @Schema(description = "實際出貨時間")
    private LocalDateTime shippedAt;

    @Schema(description = "預計送達日期")
    private LocalDate estimatedDeliveryDate;

    // 配送狀態
    @Schema(description = "配送狀態")
    private DeliveryStatus deliveryStatus;

    @Schema(description = "配送狀態描述")
    private String deliveryStatusDescription;

    @Schema(description = "實際送達時間")
    private LocalDateTime actualDeliveryDate;

    @Schema(description = "物流備註")
    private String notes;

    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
