package com.mimimart.api.dto.banner;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 更新輪播圖請求 DTO (不更新圖片)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBannerRequest {

    /**
     * 輪播圖 ID
     */
    @NotNull(message = "輪播圖 ID 不能為空")
    private Long bannerId;

    /**
     * 輪播圖標題 (可選)
     */
    @Size(max = 100, message = "輪播圖標題長度不能超過 100 個字元")
    private String title;

    /**
     * 點擊跳轉連結 (可選)
     */
    @Size(max = 500, message = "連結長度不能超過 500 個字元")
    private String linkUrl;

    /**
     * 顯示順序 (可選)
     */
    @Min(value = 0, message = "顯示順序必須大於或等於 0")
    private Integer displayOrder;
}
