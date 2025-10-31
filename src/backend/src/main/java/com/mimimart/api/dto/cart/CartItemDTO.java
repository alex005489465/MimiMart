package com.mimimart.api.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 購物車項目 DTO（包含商品完整資訊）
 */
@Getter
@NoArgsConstructor
public class CartItemDTO {
    @Setter
    private Long id;

    @Setter
    private Long productId;

    @Setter
    private String productName;

    private BigDecimal productPrice;

    @Setter
    private String productImage;

    private Integer quantity;

    @Setter
    private BigDecimal subtotal; // 小計 = 單價 × 數量

    // Custom Constructor with calculation
    public CartItemDTO(Long id, Long productId, String productName,
                       BigDecimal productPrice, String productImage, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
        this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Custom setter with calculation logic
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
        // 更新小計
        if (this.quantity != null) {
            this.subtotal = productPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    // Custom setter with calculation logic
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // 更新小計
        if (this.productPrice != null) {
            this.subtotal = this.productPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
