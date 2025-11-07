package com.mimimart.api.dto.banner;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 建立輪播圖請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBannerRequest {

    /**
     * 輪播圖標題
     */
    @NotBlank(message = "輪播圖標題不能為空")
    @Size(max = 100, message = "輪播圖標題長度不能超過 100 個字元")
    private String title;

    /**
     * 點擊跳轉連結 (可選)
     */
    @Size(max = 500, message = "連結長度不能超過 500 個字元")
    private String linkUrl;

    /**
     * 顯示順序
     */
    @NotNull(message = "顯示順序不能為空")
    @Min(value = 0, message = "顯示順序必須大於或等於 0")
    private Integer displayOrder;

    /**
     * 上架時間 (選填，NULL 表示立即上架)
     */
    private LocalDateTime publishedAt;

    /**
     * 下架時間 (選填，NULL 表示永不下架)
     */
    private LocalDateTime unpublishedAt;
}
