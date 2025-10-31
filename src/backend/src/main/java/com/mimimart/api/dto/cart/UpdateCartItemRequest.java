package com.mimimart.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 更新購物車項目數量請求 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {
    private Long productId;
    private Integer quantity;
}
