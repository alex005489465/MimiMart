package com.mimimart.api.dto.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 記錄出貨資訊請求 DTO（後台管理員填寫）
 */
@Data
@Schema(description = "記錄出貨資訊請求")
public class RecordShippingRequest {
    @NotBlank(message = "訂單編號不可為空")
    @Schema(description = "訂單編號", example = "ORD1234567890")
    private String orderNumber;

    @NotBlank(message = "物流商名稱不可為空")
    @Schema(description = "物流商名稱", example = "黑貓宅急便")
    private String carrier;

    @NotBlank(message = "物流追蹤號碼不可為空")
    @Schema(description = "物流追蹤號碼", example = "123456789")
    private String trackingNumber;

    @Schema(description = "預計送達日期", example = "2025-01-15")
    private LocalDate estimatedDeliveryDate;
}
