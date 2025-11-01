package com.mimimart.api.dto.order;

import jakarta.validation.constraints.NotBlank;

/**
 * 訂單編號請求(用於取代路徑參數)
 */
public class OrderNumberRequest {

    @NotBlank(message = "訂單編號不可為空")
    private String orderNumber;

    // Constructors
    public OrderNumberRequest() {
    }

    public OrderNumberRequest(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
