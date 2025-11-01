package com.mimimart.api.dto.order;

import com.mimimart.domain.order.model.OrderItem;

import java.math.BigDecimal;

/**
 * 訂單項目回應
 */
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private BigDecimal productOriginalPrice;
    private String productImage;
    private Integer quantity;
    private BigDecimal subtotal;

    /**
     * 從領域模型建立回應 DTO
     */
    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.productId = item.getProductId();
        response.productName = item.getSnapshot().getProductName();
        response.productPrice = item.getSnapshot().getPrice().getAmount();
        response.productOriginalPrice = item.getSnapshot().getOriginalPrice().getAmount();
        response.productImage = item.getSnapshot().getProductImage();
        response.quantity = item.getQuantity();
        response.subtotal = item.getSubtotal().getAmount();
        return response;
    }

    // Getters
    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public BigDecimal getProductOriginalPrice() {
        return productOriginalPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
