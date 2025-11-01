package com.mimimart.infrastructure.persistence.entity;

/**
 * 付款狀態枚舉
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public enum PaymentStatus {
    /**
     * 等待付款中
     */
    PENDING_PAYMENT,

    /**
     * 已付款
     */
    PAID,

    /**
     * 已逾期
     */
    EXPIRED,

    /**
     * 已取消
     */
    CANCELLED
}
