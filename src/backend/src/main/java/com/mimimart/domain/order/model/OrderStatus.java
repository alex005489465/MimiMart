package com.mimimart.domain.order.model;

/**
 * 訂單狀態枚舉
 */
public enum OrderStatus {
    /**
     * 等待付款中
     */
    PAYMENT_PENDING("等待付款中"),

    /**
     * 已付款
     */
    PAID("已付款"),

    /**
     * 已出貨
     */
    SHIPPED("已出貨"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已取消
     */
    CANCELLED("已取消");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 判斷是否可以取消
     * 只有等待付款中的訂單可以取消
     */
    public boolean isCancellable() {
        return this == PAYMENT_PENDING;
    }

    /**
     * 判斷是否可以出貨
     * 只有已付款的訂單可以出貨
     */
    public boolean isShippable() {
        return this == PAID;
    }

    /**
     * 判斷是否已完成或已取消(終態)
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED;
    }
}
