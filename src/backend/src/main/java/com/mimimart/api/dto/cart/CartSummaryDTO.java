package com.mimimart.api.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 購物車摘要 DTO（包含所有項目與總計資訊）
 */
@Getter
@NoArgsConstructor
public class CartSummaryDTO {
    private List<CartItemDTO> items;

    @Setter
    private Integer totalQuantity; // 總件數

    @Setter
    private BigDecimal totalAmount; // 總金額

    // TODO: 預留未來擴充欄位
    // private BigDecimal discount; // 折扣金額
    // private BigDecimal shippingFee; // 運費
    // private BigDecimal finalAmount; // 最終應付金額

    // Custom Constructor with calculation
    public CartSummaryDTO(List<CartItemDTO> items) {
        this.items = items;
        this.calculateTotals();
    }

    /**
     * 計算總件數與總金額
     */
    private void calculateTotals() {
        this.totalQuantity = items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
        this.totalAmount = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Custom setter with calculation logic
    public void setItems(List<CartItemDTO> items) {
        this.items = items;
        this.calculateTotals();
    }
}
