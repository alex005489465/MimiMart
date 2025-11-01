package com.mimimart.api.dto.banner;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 更新輪播圖順序請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {

    /**
     * 輪播圖 ID
     */
    @NotNull(message = "輪播圖 ID 不能為空")
    private Long bannerId;

    /**
     * 新的顯示順序
     */
    @NotNull(message = "顯示順序不能為空")
    @Min(value = 0, message = "顯示順序必須大於或等於 0")
    private Integer displayOrder;
}
