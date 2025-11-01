package com.mimimart.api.dto.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * 付款詳情查詢請求 DTO
 * 用於查詢付款相關資訊
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (Payment Integration)
 */
public class PaymentDetailRequest {

    @NotBlank(message = "付款編號不能為空")
    private String paymentNumber;

    public PaymentDetailRequest() {
    }

    public PaymentDetailRequest(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }
}
