package com.mimimart.api.controller.shop;

import com.mimimart.fixtures.PaymentTestFixtures;
import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.config.ECPayConfig;
import com.mimimart.infrastructure.persistence.entity.*;
import com.mimimart.infrastructure.persistence.repository.OrderRepository;
import com.mimimart.infrastructure.persistence.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ECPayCallbackController 測試類別
 * 測試綠界付款回調 API 端點
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("綠界付款回調 API 測試")
class ECPayCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestFixtures testFixtures;

    @Autowired
    private PaymentTestFixtures paymentTestFixtures;

    @Autowired
    private ECPayConfig ecPayConfig;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Member testMember;
    private OrderEntity testOrder;
    private PaymentEntity testPayment;

    @BeforeEach
    void setUp() {
        // 建立測試用會員
        testMember = testFixtures.createTestMember(1);

        // 建立測試訂單
        testOrder = paymentTestFixtures.createTestOrder(
                testMember.getId(),
                1,
                BigDecimal.valueOf(1000)
        );

        // 建立測試付款
        testPayment = paymentTestFixtures.createTestPayment(
                testOrder.getId(),
                1,
                BigDecimal.valueOf(1000),
                "Credit"
        );
    }

    @Test
    @DisplayName("GET /api/shop/payment/ecpay/up - 健康檢查端點正常回應")
    void testHealthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/shop/payment/ecpay/up"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ECPay endpoint is UP!")));
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/callback - 處理成功付款回調")
    void testHandleCallback_Success() throws Exception {
        // Given: 模擬綠界成功付款回調參數
        Map<String, String> callbackParams = paymentTestFixtures.createMockECPaySuccessCallback(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount(),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callbackParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("1|OK"));

        // 驗證付款狀態已更新為已付款
        Optional<PaymentEntity> updatedPayment = paymentRepository.findById(testPayment.getId());
        assertThat(updatedPayment).isPresent();
        assertThat(updatedPayment.get().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updatedPayment.get().getExternalTransactionId()).isNotNull();
        assertThat(updatedPayment.get().getPaidAt()).isNotNull();

        // 驗證訂單狀態已更新為已付款
        Optional<OrderEntity> updatedOrder = orderRepository.findById(testOrder.getId());
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getStatus()).isEqualTo(com.mimimart.domain.order.model.OrderStatus.PAID);
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/callback - 處理失敗付款回調")
    void testHandleCallback_Failed() throws Exception {
        // Given: 模擬綠界失敗付款回調參數
        Map<String, String> callbackParams = paymentTestFixtures.createMockECPayFailedCallback(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount(),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callbackParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("1|OK"));

        // 驗證付款狀態仍為待付款(失敗不更新狀態)
        Optional<PaymentEntity> updatedPayment = paymentRepository.findById(testPayment.getId());
        assertThat(updatedPayment).isPresent();
        assertThat(updatedPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/callback - 簽名驗證失敗時返回錯誤")
    void testHandleCallback_InvalidSignature() throws Exception {
        // Given: 建立錯誤的檢查碼回調參數
        Map<String, String> callbackParams = paymentTestFixtures.createInvalidCheckMacValueCallback(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callbackParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("0|")));

        // 驗證付款狀態未變更
        Optional<PaymentEntity> updatedPayment = paymentRepository.findById(testPayment.getId());
        assertThat(updatedPayment).isPresent();
        assertThat(updatedPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/callback - 重複付款的冪等性驗證")
    void testHandleCallback_IdempotentCheck() throws Exception {
        // Given: 模擬綠界成功付款回調參數
        Map<String, String> callbackParams = paymentTestFixtures.createMockECPaySuccessCallback(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount(),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callbackParams.forEach(params::add);

        // When: 第一次回調
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().string("1|OK"));

        // 驗證付款已標記為已付款
        Optional<PaymentEntity> firstUpdate = paymentRepository.findById(testPayment.getId());
        assertThat(firstUpdate.get().getStatus()).isEqualTo(PaymentStatus.PAID);

        // When: 第二次回調(重複)
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().string("1|OK"));

        // Then: 驗證狀態保持一致(冪等性)
        Optional<PaymentEntity> secondUpdate = paymentRepository.findById(testPayment.getId());
        assertThat(secondUpdate.get().getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/callback - 金額不符時返回錯誤")
    void testHandleCallback_AmountMismatch() throws Exception {
        // Given: 模擬金額不符的回調參數(金額故意設定錯誤)
        Map<String, String> callbackParams = paymentTestFixtures.createMockECPaySuccessCallback(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                BigDecimal.valueOf(9999), // 錯誤的金額
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callbackParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("0|")));

        // 驗證付款狀態未變更
        Optional<PaymentEntity> updatedPayment = paymentRepository.findById(testPayment.getId());
        assertThat(updatedPayment).isPresent();
        assertThat(updatedPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/callback - 付款編號不存在時返回錯誤")
    void testHandleCallback_PaymentNotFound() throws Exception {
        // Given: 模擬不存在的付款編號回調
        Map<String, String> callbackParams = paymentTestFixtures.createMockECPaySuccessCallback(
                ecPayConfig.getMerchantId(),
                "NON_EXISTENT_PAYMENT_NUMBER",
                BigDecimal.valueOf(1000),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callbackParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/callback")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("0|")));
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/return - 付款成功返回頁面正確產生")
    void testHandleReturn_Success() throws Exception {
        // Given: 模擬綠界返回參數
        Map<String, String> returnParams = paymentTestFixtures.createMockECPayReturnParams(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount(),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        returnParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/return")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("付款成功")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("秒後自動跳轉")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(testPayment.getPaymentNumber())));
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/return - 付款失敗返回頁面正確產生")
    void testHandleReturn_Failed() throws Exception {
        // Given: 模擬綠界失敗返回參數
        Map<String, String> returnParams = paymentTestFixtures.createMockECPayFailedCallback(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount(),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        returnParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/return")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("付款失敗")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("秒後自動返回")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("付款失敗")));
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/return - 返回頁面包含倒數跳轉邏輯")
    void testHandleReturn_ContainsRedirectLogic() throws Exception {
        // Given: 模擬綠界返回參數
        Map<String, String> returnParams = paymentTestFixtures.createMockECPayReturnParams(
                ecPayConfig.getMerchantId(),
                testPayment.getPaymentNumber(),
                testPayment.getAmount(),
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        returnParams.forEach(params::add);

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/return")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("countdown")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("setInterval")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("redirectNow")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("window.location.href")));
    }
}
