package com.mimimart.api.dto.order;

import com.mimimart.api.dto.shipment.ShipmentResponse;
import com.mimimart.domain.order.model.Order;
import com.mimimart.domain.order.model.OrderStatus;
import com.mimimart.domain.shipment.model.Shipment;

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

    // 物流資訊
    private ShipmentResponse shipment;

    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 從領域模型建立回應 DTO
     */
    public static OrderDetailResponse from(Order order, Shipment shipment) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.id = order.getId();
        response.orderNumber = order.getOrderNumber().getValue();
        response.status = order.getStatus();
        response.statusDisplayName = order.getStatus().getDisplayName();
        response.items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
        response.totalAmount = order.getTotalAmount().getAmount();

        // 物流資訊
        response.shipment = ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .receiverName(shipment.getReceiverName())
                .receiverPhone(shipment.getReceiverPhone())
                .shippingAddress(shipment.getShippingAddress())
                .deliveryMethod(shipment.getDeliveryMethod())
                .deliveryNote(shipment.getDeliveryNote())
                .shippingFee(shipment.getShippingFee())
                .carrier(shipment.getCarrier())
                .trackingNumber(shipment.getTrackingNumber())
                .shippedAt(shipment.getShippedAt())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .deliveryStatus(shipment.getDeliveryStatus())
                .deliveryStatusDescription(shipment.getDeliveryStatus().getDescription())
                .actualDeliveryDate(shipment.getActualDeliveryDate())
                .notes(shipment.getNotes())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();

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

    public ShipmentResponse getShipment() {
        return shipment;
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
