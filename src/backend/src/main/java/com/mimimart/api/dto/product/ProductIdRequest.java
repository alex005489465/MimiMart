package com.mimimart.api.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 商品 ID 請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductIdRequest {

    @NotNull(message = "商品 ID 不能為空")
    private Long productId;
}
