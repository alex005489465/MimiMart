package com.mimimart.api.dto.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新商品請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @NotNull(message = "商品 ID 不能為空")
    private Long productId;

    @NotBlank(message = "商品名稱不能為空")
    @Size(max = 200, message = "商品名稱長度不能超過 200 個字元")
    private String name;

    private String description;

    @NotNull(message = "商品售價不能為空")
    @DecimalMin(value = "0.01", message = "商品售價必須大於 0")
    @DecimalMax(value = "99999999.99", message = "商品售價不能超過 99,999,999.99")
    @Digits(integer = 8, fraction = 2, message = "商品售價格式不正確 (最大 99,999,999.99)")
    private BigDecimal price;

    @Size(max = 500, message = "圖片 URL 長度不能超過 500 個字元")
    private String imageUrl;

    @NotNull(message = "分類 ID 不能為空")
    private Long categoryId;

    /**
     * 上架時間 (NULL 表示不限制)
     */
    private LocalDateTime publishedAt;

    /**
     * 下架時間 (NULL 表示不限制)
     */
    private LocalDateTime unpublishedAt;
}
