package com.mimimart.domain.order.event;

import java.math.BigDecimal;

/**
 * 訂單建立事件
 * 當訂單成功建立後發布此事件,觸發後續流程(如建立付款記錄)
 */
public class OrderCreatedEvent {
    private final String orderNumber;
    private final Long memberId;
    private final BigDecimal totalAmount;

    public OrderCreatedEvent(String orderNumber, Long memberId, BigDecimal totalAmount) {
        this.orderNumber = orderNumber;
        this.memberId = memberId;
        this.totalAmount = totalAmount;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getMemberId() {
        return memberId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
               "orderNumber='" + orderNumber + '\'' +
               ", memberId=" + memberId +
               ", totalAmount=" + totalAmount +
               '}';
    }
}
