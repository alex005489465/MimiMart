package com.mimimart.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 購物車摘要 DTO（包含所有項目與總計資訊）
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDTO {

    private List<CartItemDTO> items;

    private Integer totalItems; // 商品種類數

    private Integer totalQuantity; // 總件數

    private BigDecimal totalAmount; // 總金額

    /**
     * 從購物車項目列表建立摘要
     */
    public static CartSummaryDTO from(List<CartItemDTO> items) {
        Integer totalItems = items.size();
        Integer totalQuantity = items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
        BigDecimal totalAmount = items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartSummaryDTO.builder()
                .items(items)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }
}
