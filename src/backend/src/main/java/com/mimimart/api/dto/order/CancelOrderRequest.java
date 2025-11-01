package com.mimimart.api.dto.order;

import jakarta.validation.constraints.NotBlank;

/**
 * 取消訂單請求(後台用)
 */
public class CancelOrderRequest {

    @NotBlank(message = "訂單編號不可為空")
    private String orderNumber;

    @NotBlank(message = "取消原因不可為空")
    private String cancellationReason;

    // Constructors
    public CancelOrderRequest() {
    }

    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
