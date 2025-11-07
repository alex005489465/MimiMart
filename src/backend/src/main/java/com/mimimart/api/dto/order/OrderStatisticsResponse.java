package com.mimimart.api.dto.order;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 訂單統計回應
 */
public class OrderStatisticsResponse {

    private Long totalOrders;
    private BigDecimal totalAmount;
    private Map<String, Long> statusDistribution;

    public OrderStatisticsResponse(Long totalOrders, BigDecimal totalAmount, Map<String, Long> statusDistribution) {
        this.totalOrders = totalOrders;
        this.totalAmount = totalAmount;
        this.statusDistribution = statusDistribution;
    }

    // Getters
    public Long getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }
}
