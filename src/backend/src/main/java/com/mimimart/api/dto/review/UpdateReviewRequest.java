package com.mimimart.api.dto.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 更新評價請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {

    @NotNull(message = "評價ID不得為空")
    private Long reviewId;

    /**
     * 新評分（1-5星，選填）
     */
    @Min(value = 1, message = "評分不得低於1星")
    @Max(value = 5, message = "評分不得高於5星")
    private Integer rating;

    /**
     * 新評價內容（選填）
     */
    @Size(max = 2000, message = "評價內容不得超過2000字")
    private String content;
}
