package com.mimimart.api.dto.order;

import com.mimimart.domain.order.model.DeliveryInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 建立訂單請求
 */
public class CreateOrderRequest {

    @NotBlank(message = "收件人姓名不可為空")
    private String receiverName;

    @NotBlank(message = "收件人電話不可為空")
    private String receiverPhone;

    @NotBlank(message = "配送地址不可為空")
    private String shippingAddress;

    @NotNull(message = "配送方式不可為空")
    private DeliveryInfo.DeliveryMethod deliveryMethod;

    private String deliveryNote;

    // Constructors
    public CreateOrderRequest() {
    }

    // Getters and Setters
    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public DeliveryInfo.DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryInfo.DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public void setDeliveryNote(String deliveryNote) {
        this.deliveryNote = deliveryNote;
    }

    /**
     * 轉換為領域值對象
     */
    public DeliveryInfo toDeliveryInfo() {
        return DeliveryInfo.builder()
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .shippingAddress(shippingAddress)
                .deliveryMethod(deliveryMethod)
                .deliveryNote(deliveryNote)
                .build();
    }
}
