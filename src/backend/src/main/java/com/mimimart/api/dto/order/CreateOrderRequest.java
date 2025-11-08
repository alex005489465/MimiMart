package com.mimimart.api.dto.order;

import com.mimimart.domain.order.model.DeliveryInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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

    @NotNull(message = "運費不可為空")
    private BigDecimal shippingFee;

    @NotNull(message = "購買項目不可為空")
    @Size(min = 1, message = "至少需要一個購買項目")
    @Valid
    private List<OrderItemRequest> items;

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

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
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

    /**
     * 訂單項目請求
     */
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品 ID 不可為空")
        private Long productId;

        @NotNull(message = "數量不可為空")
        @Min(value = 1, message = "數量至少為 1")
        @Max(value = 999, message = "數量不可超過 999")
        private Integer quantity;
    }
}
