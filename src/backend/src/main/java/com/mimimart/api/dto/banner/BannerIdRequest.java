package com.mimimart.api.dto.banner;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 輪播圖 ID 請求 DTO
 * 用於刪除、啟用、停用等操作
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BannerIdRequest {

    /**
     * 輪播圖 ID
     */
    @NotNull(message = "輪播圖 ID 不能為空")
    private Long bannerId;
}
