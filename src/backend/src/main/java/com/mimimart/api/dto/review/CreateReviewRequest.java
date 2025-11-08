package com.mimimart.api.dto.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 新增評價請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "商品ID不得為空")
    private Long productId;

    @NotNull(message = "訂單項目ID不得為空")
    private Long orderItemId;

    /**
     * 評分（1-5星）
     * DTO 層第一道防線：Bean Validation
     */
    @NotNull(message = "評分不得為空")
    @Min(value = 1, message = "評分不得低於1星")
    @Max(value = 5, message = "評分不得高於5星")
    private Integer rating;

    /**
     * 評價內容（選填）
     */
    @Size(max = 2000, message = "評價內容不得超過2000字")
    private String content;
}
