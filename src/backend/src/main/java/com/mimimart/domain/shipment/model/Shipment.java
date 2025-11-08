package com.mimimart.domain.shipment.model;

import com.mimimart.domain.order.model.DeliveryInfo;
import com.mimimart.domain.shipment.exception.InvalidDeliveryStatusTransitionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流聚合根
 * 封裝物流業務邏輯與配送狀態轉換規則
 */
public class Shipment {
    private Long id;
    private final Long orderId;

    // 會員填寫的配送資訊
    private final String receiverName;
    private final String receiverPhone;
    private final String shippingAddress;
    private final String deliveryMethod;
    private final String deliveryNote;
    private final BigDecimal shippingFee;

    // 管理員填寫的物流資訊
    private String carrier;
    private String trackingNumber;
    private LocalDateTime shippedAt;
    private LocalDate estimatedDeliveryDate;

    // 配送狀態管理
    private DeliveryStatus deliveryStatus;
    private LocalDateTime actualDeliveryDate;
    private String notes;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Shipment(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.receiverName = builder.receiverName;
        this.receiverPhone = builder.receiverPhone;
        this.shippingAddress = builder.shippingAddress;
        this.deliveryMethod = builder.deliveryMethod;
        this.deliveryNote = builder.deliveryNote;
        this.shippingFee = builder.shippingFee;
        this.carrier = builder.carrier;
        this.trackingNumber = builder.trackingNumber;
        this.shippedAt = builder.shippedAt;
        this.estimatedDeliveryDate = builder.estimatedDeliveryDate;
        this.deliveryStatus = builder.deliveryStatus;
        this.actualDeliveryDate = builder.actualDeliveryDate;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 記錄出貨資訊（後台管理員填寫物流商、追蹤號碼等）
     */
    public void recordShipping(ShippingInfo shippingInfo) {
        if (!deliveryStatus.canShip()) {
            throw new InvalidDeliveryStatusTransitionException(
                "配送狀態不正確: 只有準備中的物流可以記錄出貨資訊 (當前狀態: " + deliveryStatus.getDescription() + ")"
            );
        }

        shippingInfo.validate();

        this.carrier = shippingInfo.getCarrier();
        this.trackingNumber = shippingInfo.getTrackingNumber();
        this.shippedAt = shippingInfo.getShippedAt();
        this.estimatedDeliveryDate = shippingInfo.getEstimatedDeliveryDate();
        this.deliveryStatus = DeliveryStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新配送狀態
     */
    public void updateStatus(DeliveryStatus newStatus, String notes) {
        if (!deliveryStatus.canTransitionTo(newStatus)) {
            throw new InvalidDeliveryStatusTransitionException(
                String.format("無效的配送狀態轉換: %s -> %s",
                    deliveryStatus.getDescription(),
                    newStatus.getDescription())
            );
        }

        this.deliveryStatus = newStatus;
        if (notes != null && !notes.isBlank()) {
            this.notes = notes;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 標記為已送達
     */
    public void markAsDelivered(LocalDateTime actualDeliveryDate) {
        if (deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY) {
            throw new InvalidDeliveryStatusTransitionException(
                "配送狀態不正確: 只有配送中的物流可以標記為已送達 (當前狀態: " + deliveryStatus.getDescription() + ")"
            );
        }

        if (actualDeliveryDate == null) {
            throw new IllegalArgumentException("實際送達時間不可為空");
        }

        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.actualDeliveryDate = actualDeliveryDate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 驗證物流是否屬於指定訂單
     */
    public boolean belongsToOrder(Long orderId) {
        return this.orderId.equals(orderId);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
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

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public LocalDateTime getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public String getNotes() {
        return notes;
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
        private Long orderId;
        private String receiverName;
        private String receiverPhone;
        private String shippingAddress;
        private String deliveryMethod;
        private String deliveryNote;
        private BigDecimal shippingFee;
        private String carrier;
        private String trackingNumber;
        private LocalDateTime shippedAt;
        private LocalDate estimatedDeliveryDate;
        private DeliveryStatus deliveryStatus = DeliveryStatus.PREPARING; // 預設狀態
        private LocalDateTime actualDeliveryDate;
        private String notes;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder receiverName(String receiverName) {
            this.receiverName = receiverName;
            return this;
        }

        public Builder receiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
            return this;
        }

        public Builder shippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public Builder deliveryMethod(String deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
            return this;
        }

        public Builder deliveryNote(String deliveryNote) {
            this.deliveryNote = deliveryNote;
            return this;
        }

        public Builder shippingFee(BigDecimal shippingFee) {
            this.shippingFee = shippingFee;
            return this;
        }

        public Builder carrier(String carrier) {
            this.carrier = carrier;
            return this;
        }

        public Builder trackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
            return this;
        }

        public Builder shippedAt(LocalDateTime shippedAt) {
            this.shippedAt = shippedAt;
            return this;
        }

        public Builder estimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
            this.estimatedDeliveryDate = estimatedDeliveryDate;
            return this;
        }

        public Builder deliveryStatus(DeliveryStatus deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public Builder actualDeliveryDate(LocalDateTime actualDeliveryDate) {
            this.actualDeliveryDate = actualDeliveryDate;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
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

        /**
         * 從 DeliveryInfo 建立 Shipment（訂單建立時使用）
         */
        public Builder fromDeliveryInfo(DeliveryInfo deliveryInfo, BigDecimal shippingFee) {
            this.receiverName = deliveryInfo.getReceiverName();
            this.receiverPhone = deliveryInfo.getReceiverPhone();
            this.shippingAddress = deliveryInfo.getShippingAddress();
            this.deliveryMethod = deliveryInfo.getDeliveryMethod().name();
            this.deliveryNote = deliveryInfo.getDeliveryNote();
            this.shippingFee = shippingFee;
            return this;
        }

        public Shipment build() {
            validate();
            return new Shipment(this);
        }

        private void validate() {
            if (orderId == null) {
                throw new IllegalArgumentException("訂單 ID 不可為 null");
            }
            if (receiverName == null || receiverName.isBlank()) {
                throw new IllegalArgumentException("收件人姓名不可為空");
            }
            if (receiverPhone == null || receiverPhone.isBlank()) {
                throw new IllegalArgumentException("收件人電話不可為空");
            }
            if (shippingAddress == null || shippingAddress.isBlank()) {
                throw new IllegalArgumentException("配送地址不可為空");
            }
            if (deliveryMethod == null || deliveryMethod.isBlank()) {
                throw new IllegalArgumentException("配送方式不可為空");
            }
            if (shippingFee == null) {
                throw new IllegalArgumentException("運費不可為 null");
            }
        }
    }
}
