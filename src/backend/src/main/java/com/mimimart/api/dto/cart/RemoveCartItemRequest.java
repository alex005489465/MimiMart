package com.mimimart.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 移除購物車項目請求 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveCartItemRequest {
    private Long productId;
}
