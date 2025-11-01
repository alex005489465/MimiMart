package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.payment.PaymentDetailRequest;
import com.mimimart.api.dto.payment.PaymentDetailResponse;
import com.mimimart.api.dto.payment.PaymentParamsResponse;
import com.mimimart.api.dto.ApiResponse;
import com.mimimart.application.service.PaymentService;
import com.mimimart.domain.payment.model.Payment;
import com.mimimart.infrastructure.config.ECPayConfig;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 前台付款控制器
 * 處理會員的付款相關操作
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (Payment Integration)
 */
@RestController
@RequestMapping("/api/shop/payment")
public class ShopPaymentController {

    private final PaymentService paymentService;
    private final ECPayConfig ecPayConfig;

    public ShopPaymentController(PaymentService paymentService, ECPayConfig ecPayConfig) {
        this.paymentService = paymentService;
        this.ecPayConfig = ecPayConfig;
    }

    /**
     * 查詢付款詳情
     * URL 使用靜態路徑,付款編號放在 Body 中
     *
     * @param request 查詢請求
     * @return 付款詳情
     */
    @PostMapping("/detail")
    public ApiResponse<PaymentDetailResponse> getPaymentDetail(@Valid @RequestBody PaymentDetailRequest request) {
        Payment payment = paymentService.getPaymentDetail(request.getPaymentNumber());
        return ApiResponse.success("查詢付款詳情成功", PaymentDetailResponse.from(payment));
    }

    /**
     * 取得綠界付款參數
     * 前端可使用這些參數自動提交表單到綠界
     *
     * @param request 查詢請求
     * @return 綠界付款參數
     */
    @PostMapping("/ecpay/params")
    public ApiResponse<PaymentParamsResponse> getECPayParams(@Valid @RequestBody PaymentDetailRequest request) {
        Map<String, String> params = paymentService.getECPayParams(request.getPaymentNumber());

        PaymentParamsResponse response = new PaymentParamsResponse();
        response.setPaymentUrl(ecPayConfig.getCreatePaymentUrl());
        response.setParams(params);

        return ApiResponse.success("取得綠界付款參數成功", response);
    }
}
