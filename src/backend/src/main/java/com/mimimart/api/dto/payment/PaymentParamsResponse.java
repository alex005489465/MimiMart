package com.mimimart.api.dto.payment;

import java.util.Map;

/**
 * 綠界付款參數回應 DTO
 * 包含付款 URL 和所有需要提交的參數
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (Payment Integration)
 */
public class PaymentParamsResponse {

    private String paymentUrl;
    private Map<String, String> params;

    public PaymentParamsResponse() {
    }

    public PaymentParamsResponse(String paymentUrl, Map<String, String> params) {
        this.paymentUrl = paymentUrl;
        this.params = params;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
