package com.mimimart.domain.order.model;

import com.mimimart.domain.order.exception.InvalidOrderStatusTransitionException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 訂單聚合根
 * 封裝訂單業務邏輯與狀態轉換規則
 */
public class Order {
    private Long id;
    private final Long memberId;
    private final OrderNumber orderNumber;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final Money totalAmount;
    private final DeliveryInfo deliveryInfo;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Order(Builder builder) {
        this.id = builder.id;
        this.memberId = builder.memberId;
        this.orderNumber = builder.orderNumber;
        this.status = builder.status;
        this.items = new ArrayList<>(builder.items);
        this.totalAmount = builder.totalAmount;
        this.deliveryInfo = builder.deliveryInfo;
        this.cancellationReason = builder.cancellationReason;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    /**
     * 建立建構器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 標記為已付款
     */
    public void markAsPaid() {
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStatusTransitionException(
                "訂單狀態不正確:只有等待付款中的訂單可以標記為已付款"
            );
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 標記為已出貨
     */
    public void ship() {
        if (!status.isShippable()) {
            throw new InvalidOrderStatusTransitionException(
                "訂單狀態不正確:只有已付款的訂單可以出貨 (當前狀態: " + status.getDisplayName() + ")"
            );
        }
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 標記為已完成
     */
    public void complete() {
        if (status != OrderStatus.SHIPPED) {
            throw new InvalidOrderStatusTransitionException(
                "訂單狀態不正確:只有已出貨的訂單可以完成"
            );
        }
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消訂單
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        if (!status.isCancellable()) {
            throw new InvalidOrderStatusTransitionException(
                "訂單狀態不正確:只有等待付款中的訂單可以取消 (當前狀態: " + status.getDisplayName() + ")"
            );
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("取消原因不可為空");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 驗證訂單是否屬於指定會員
     */
    public boolean belongsToMember(Long memberId) {
        return this.memberId.equals(memberId);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public OrderNumber getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
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

    /**
     * 建構器
     */
    public static class Builder {
        private Long id;
        private Long memberId;
        private OrderNumber orderNumber;
        private OrderStatus status = OrderStatus.PAYMENT_PENDING; // 預設狀態
        private List<OrderItem> items = new ArrayList<>();
        private Money totalAmount;
        private DeliveryInfo deliveryInfo;
        private String cancellationReason;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder orderNumber(OrderNumber orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder deliveryInfo(DeliveryInfo deliveryInfo) {
            this.deliveryInfo = deliveryInfo;
            return this;
        }

        public Builder cancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Order build() {
            validate();
            return new Order(this);
        }

        private void validate() {
            if (memberId == null) {
                throw new IllegalArgumentException("會員 ID 不可為 null");
            }
            if (orderNumber == null) {
                throw new IllegalArgumentException("訂單編號不可為 null");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("訂單項目不可為空");
            }
            if (totalAmount == null) {
                throw new IllegalArgumentException("訂單總金額不可為 null");
            }
            if (deliveryInfo == null) {
                throw new IllegalArgumentException("送貨資訊不可為 null");
            }
        }
    }
}
