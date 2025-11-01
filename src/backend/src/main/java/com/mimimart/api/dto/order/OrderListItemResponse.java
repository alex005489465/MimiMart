package com.mimimart.api.dto.order;

import com.mimimart.domain.order.model.Order;
import com.mimimart.domain.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 訂單列表項目回應(簡化版)
 */
public class OrderListItemResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private LocalDateTime createdAt;

    /**
     * 從領域模型建立回應 DTO
     */
    public static OrderListItemResponse from(Order order) {
        OrderListItemResponse response = new OrderListItemResponse();
        response.id = order.getId();
        response.orderNumber = order.getOrderNumber().getValue();
        response.status = order.getStatus();
        response.statusDisplayName = order.getStatus().getDisplayName();
        response.totalAmount = order.getTotalAmount().getAmount();
        response.itemCount = order.getItems().size();
        response.createdAt = order.getCreatedAt();
        return response;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
