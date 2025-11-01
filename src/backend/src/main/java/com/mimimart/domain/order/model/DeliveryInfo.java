package com.mimimart.domain.order.model;

import java.util.Objects;

/**
 * 送貨資訊值對象
 * 封裝送貨資訊與驗證邏輯
 */
public class DeliveryInfo {
    private final String receiverName;
    private final String receiverPhone;
    private final String shippingAddress;
    private final DeliveryMethod deliveryMethod;
    private final String deliveryNote; // 可選

    private DeliveryInfo(Builder builder) {
        this.receiverName = builder.receiverName;
        this.receiverPhone = builder.receiverPhone;
        this.shippingAddress = builder.shippingAddress;
        this.deliveryMethod = builder.deliveryMethod;
        this.deliveryNote = builder.deliveryNote;
    }

    /**
     * 建立建構器
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryInfo that = (DeliveryInfo) o;
        return Objects.equals(receiverName, that.receiverName) &&
               Objects.equals(receiverPhone, that.receiverPhone) &&
               Objects.equals(shippingAddress, that.shippingAddress) &&
               deliveryMethod == that.deliveryMethod &&
               Objects.equals(deliveryNote, that.deliveryNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiverName, receiverPhone, shippingAddress, deliveryMethod, deliveryNote);
    }

    /**
     * 建構器
     */
    public static class Builder {
        private String receiverName;
        private String receiverPhone;
        private String shippingAddress;
        private DeliveryMethod deliveryMethod;
        private String deliveryNote;

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

        public Builder deliveryMethod(DeliveryMethod deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
            return this;
        }

        public Builder deliveryNote(String deliveryNote) {
            this.deliveryNote = deliveryNote;
            return this;
        }

        public DeliveryInfo build() {
            validate();
            return new DeliveryInfo(this);
        }

        private void validate() {
            if (receiverName == null || receiverName.isBlank()) {
                throw new IllegalArgumentException("收件人姓名不可為空");
            }
            if (receiverPhone == null || receiverPhone.isBlank()) {
                throw new IllegalArgumentException("收件人電話不可為空");
            }
            if (shippingAddress == null || shippingAddress.isBlank()) {
                throw new IllegalArgumentException("配送地址不可為空");
            }
            if (deliveryMethod == null) {
                throw new IllegalArgumentException("配送方式不可為空");
            }

            // 驗證手機號碼格式(台灣手機號碼)
            if (!receiverPhone.matches("^09\\d{8}$")) {
                throw new IllegalArgumentException("手機號碼格式錯誤(應為09開頭的10位數字)");
            }
        }
    }

    /**
     * 配送方式枚舉
     */
    public enum DeliveryMethod {
        HOME_DELIVERY("宅配"),
        STORE_PICKUP("超商取貨");

        private final String displayName;

        DeliveryMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
