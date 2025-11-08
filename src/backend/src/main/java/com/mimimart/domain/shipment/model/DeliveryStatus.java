package com.mimimart.domain.shipment.model;

/**
 * 配送狀態枚舉
 */
public enum DeliveryStatus {
    /**
     * 準備中（訂單建立時的初始狀態）
     */
    PREPARING("準備中"),

    /**
     * 已出貨（管理員填寫物流資訊後）
     */
    SHIPPED("已出貨"),

    /**
     * 運送中
     */
    IN_TRANSIT("運送中"),

    /**
     * 配送中
     */
    OUT_FOR_DELIVERY("配送中"),

    /**
     * 已送達
     */
    DELIVERED("已送達"),

    /**
     * 配送失敗
     */
    FAILED("配送失敗");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判斷是否可以標記為已出貨
     */
    public boolean canShip() {
        return this == PREPARING;
    }

    /**
     * 判斷是否可以更新狀態
     */
    public boolean canUpdateStatus() {
        return this != DELIVERED && this != FAILED;
    }

    /**
     * 判斷是否為最終狀態
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == FAILED;
    }

    /**
     * 驗證狀態轉換是否合法
     */
    public boolean canTransitionTo(DeliveryStatus newStatus) {
        // 最終狀態不可再轉換
        if (this.isFinalStatus()) {
            return false;
        }

        return switch (this) {
            case PREPARING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == IN_TRANSIT || newStatus == FAILED;
            case IN_TRANSIT -> newStatus == OUT_FOR_DELIVERY || newStatus == FAILED;
            case OUT_FOR_DELIVERY -> newStatus == DELIVERED || newStatus == FAILED;
            default -> false;
        };
    }
}
