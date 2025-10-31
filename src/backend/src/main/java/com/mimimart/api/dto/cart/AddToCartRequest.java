package com.mimimart.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 加入購物車請求 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private Long productId;
    private Integer quantity;
}
