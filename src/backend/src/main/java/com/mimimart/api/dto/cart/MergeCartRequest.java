package com.mimimart.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 合併購物車請求 DTO（登入時將前端購物車同步至後端）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MergeCartRequest {
    private List<MergeCartItem> items;

    /**
     * 合併購物車項目（前端 LocalStorage 資料格式）
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeCartItem {
        private Long productId;
        private Integer quantity;
    }
}
