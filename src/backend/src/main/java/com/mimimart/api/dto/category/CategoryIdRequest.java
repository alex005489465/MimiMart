package com.mimimart.api.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 分類 ID 請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryIdRequest {

    @NotNull(message = "分類 ID 不能為空")
    private Long categoryId;
}
