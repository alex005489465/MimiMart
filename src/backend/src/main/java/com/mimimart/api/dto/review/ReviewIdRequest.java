package com.mimimart.api.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 評價ID請求 DTO
 * 用於刪除、隱藏、顯示等單一ID操作
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewIdRequest {

    @NotNull(message = "評價ID不得為空")
    private Long reviewId;
}
