package com.mimimart.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 購物車項目 DTO（包含商品完整資訊與庫存狀態）
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    // 基本資訊
    private Long productId;
    private String productName;
    private Integer quantity;

    // 價格資訊
    private BigDecimal price;
    private BigDecimal totalPrice; // price * quantity

    // 展示資訊
    private String imageUrl;
    private String categoryName;

    // 庫存資訊
    private Integer stock;
    private Boolean isOutOfStock; // stock < quantity

    // 時間資訊
    private LocalDateTime addedAt;
}
