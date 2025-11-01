package com.mimimart.api.dto.order;

import com.mimimart.domain.order.model.DeliveryInfo;
import com.mimimart.domain.order.model.Order;
import com.mimimart.domain.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 訂單詳情回應
 */
public class OrderDetailResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;

    // 送貨資訊
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String deliveryMethod;
    private String deliveryNote;

    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 從領域模型建立回應 DTO
     */
    public static OrderDetailResponse from(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.id = order.getId();
        response.orderNumber = order.getOrderNumber().getValue();
        response.status = order.getStatus();
        response.statusDisplayName = order.getStatus().getDisplayName();
        response.items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
        response.totalAmount = order.getTotalAmount().getAmount();

        // 送貨資訊
        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        response.receiverName = deliveryInfo.getReceiverName();
        response.receiverPhone = deliveryInfo.getReceiverPhone();
        response.shippingAddress = deliveryInfo.getShippingAddress();
        response.deliveryMethod = deliveryInfo.getDeliveryMethod().getDisplayName();
        response.deliveryNote = deliveryInfo.getDeliveryNote();

        response.cancellationReason = order.getCancellationReason();
        response.createdAt = order.getCreatedAt();
        response.updatedAt = order.getUpdatedAt();

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

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
