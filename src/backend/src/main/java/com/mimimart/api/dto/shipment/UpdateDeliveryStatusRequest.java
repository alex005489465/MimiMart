package com.mimimart.api.dto.shipment;

import com.mimimart.domain.shipment.model.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新配送狀態請求 DTO
 */
@Data
@Schema(description = "更新配送狀態請求")
public class UpdateDeliveryStatusRequest {
    @NotBlank(message = "訂單編號不可為空")
    @Schema(description = "訂單編號", example = "ORD1234567890")
    private String orderNumber;

    @NotNull(message = "配送狀態不可為空")
    @Schema(description = "配送狀態", example = "IN_TRANSIT")
    private DeliveryStatus status;

    @Schema(description = "備註", example = "已送達配送中心")
    private String notes;
}
