package com.mimimart.api.dto.payment;

import com.mimimart.domain.payment.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 付款詳情回應 DTO
 * 用於回傳付款詳細資訊給前端
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (Payment Integration)
 */
public class PaymentDetailResponse {

    private Long id;
    private Long orderId;
    private String paymentNumber;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private String externalTransactionId;
    private Instant paidAt;
    private Instant expiredAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentDetailResponse from(Payment payment) {
        PaymentDetailResponse response = new PaymentDetailResponse();
        response.id = payment.getId();
        response.orderId = payment.getOrderId();
        response.paymentNumber = payment.getPaymentNumber().getValue();
        response.paymentMethod = payment.getPaymentMethod();
        response.amount = payment.getAmount().getAmount();
        response.status = payment.getStatus().name();
        response.externalTransactionId = payment.getExternalTransactionId();
        response.paidAt = payment.getPaidAt();
        response.expiredAt = payment.getExpiredAt();
        response.createdAt = payment.getCreatedAt();
        response.updatedAt = payment.getUpdatedAt();
        return response;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
