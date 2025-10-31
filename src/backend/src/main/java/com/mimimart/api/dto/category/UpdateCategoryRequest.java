package com.mimimart.api.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 更新分類請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @NotNull(message = "分類 ID 不能為空")
    private Long categoryId;

    @NotBlank(message = "分類名稱不能為空")
    @Size(max = 100, message = "分類名稱長度不能超過 100 個字元")
    private String name;

    @Size(max = 500, message = "分類描述長度不能超過 500 個字元")
    private String description;

    private Integer sortOrder;
}
