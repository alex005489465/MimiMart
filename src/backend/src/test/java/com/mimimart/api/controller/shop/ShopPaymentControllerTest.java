package com.mimimart.api.controller.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.api.dto.payment.PaymentDetailRequest;
import com.mimimart.fixtures.PaymentTestFixtures;
import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.config.ECPayConfig;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import com.mimimart.infrastructure.persistence.entity.PaymentEntity;
import com.mimimart.infrastructure.security.CustomUserDetails;
import com.mimimart.shared.valueobject.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShopPaymentController 測試類別
 * 測試付款 API 端點
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("付款 API 測試")
class ShopPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestFixtures testFixtures;

    @Autowired
    private PaymentTestFixtures paymentTestFixtures;

    @Autowired
    private ECPayConfig ecPayConfig;

    private Member testMember;
    private CustomUserDetails userDetails;
    private OrderEntity testOrder;
    private PaymentEntity testPayment;

    @BeforeEach
    void setUp() {
        // 建立測試用會員
        testMember = testFixtures.createTestMember(1);

        // 建立 UserDetails 用於認證
        userDetails = new CustomUserDetails(
                testMember.getId(),
                testMember.getEmail(),
                testMember.getPasswordHash(),
                Collections.emptyList(),
                UserType.MEMBER
        );

        // 建立測試訂單和付款
        testOrder = paymentTestFixtures.createTestOrder(
                testMember.getId(),
                1,
                BigDecimal.valueOf(1000)
        );

        testPayment = paymentTestFixtures.createTestPayment(
                testOrder.getId(),
                1,
                BigDecimal.valueOf(1000),
                "Credit"
        );
    }

    @Test
    @DisplayName("POST /api/shop/payment/detail - 成功查詢付款詳情")
    void testGetPaymentDetail_Success() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest(testPayment.getPaymentNumber());

        // When & Then
        mockMvc.perform(post("/api/shop/payment/detail")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查詢付款詳情成功"))
                .andExpect(jsonPath("$.data.paymentNumber").value(testPayment.getPaymentNumber()))
                .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.amount").value(1000))
                .andExpect(jsonPath("$.data.paymentMethod").value("Credit"))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"));
    }

    @Test
    @DisplayName("POST /api/shop/payment/detail - 付款編號為空時返回錯誤")
    void testGetPaymentDetail_EmptyPaymentNumber() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest("");

        // When & Then: Spring Boot 的 @NotBlank 驗證會在綁定時失敗,但可能返回 200 並在業務層處理
        mockMvc.perform(post("/api/shop/payment/detail")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/shop/payment/detail - 付款不存在時返回錯誤")
    void testGetPaymentDetail_PaymentNotFound() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest("NON_EXISTENT_PAYMENT");

        // When & Then
        mockMvc.perform(post("/api/shop/payment/detail")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/params - 成功取得綠界付款參數")
    void testGetECPayParams_Success() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest(testPayment.getPaymentNumber());

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/params")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("取得綠界付款參數成功"))
                .andExpect(jsonPath("$.data.paymentUrl").value(ecPayConfig.getCreatePaymentUrl()))
                .andExpect(jsonPath("$.data.params").isMap())
                .andExpect(jsonPath("$.data.params.MerchantID").value(ecPayConfig.getMerchantId()))
                .andExpect(jsonPath("$.data.params.MerchantTradeNo").value(testPayment.getPaymentNumber()))
                .andExpect(jsonPath("$.data.params.TotalAmount").value("1000"))
                .andExpect(jsonPath("$.data.params.CheckMacValue").exists());
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/params - 驗證綠界參數的必要欄位")
    void testGetECPayParams_ValidateRequiredFields() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest(testPayment.getPaymentNumber());

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/params")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.params.MerchantID").exists())
                .andExpect(jsonPath("$.data.params.MerchantTradeNo").exists())
                .andExpect(jsonPath("$.data.params.TotalAmount").exists())
                .andExpect(jsonPath("$.data.params.TradeDesc").exists())
                .andExpect(jsonPath("$.data.params.ItemName").exists())
                .andExpect(jsonPath("$.data.params.ReturnURL").exists())
                .andExpect(jsonPath("$.data.params.ClientBackURL").exists())
                .andExpect(jsonPath("$.data.params.ChoosePayment").exists())
                .andExpect(jsonPath("$.data.params.EncryptType").exists())
                .andExpect(jsonPath("$.data.params.CheckMacValue").exists());
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/params - 付款編號為空時返回錯誤")
    void testGetECPayParams_EmptyPaymentNumber() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest("");

        // When & Then: Spring Boot 的 @NotBlank 驗證會在綁定時失敗,但可能返回 200 並在業務層處理
        mockMvc.perform(post("/api/shop/payment/ecpay/params")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/params - 付款不存在時返回錯誤")
    void testGetECPayParams_PaymentNotFound() throws Exception {
        // Given
        PaymentDetailRequest request = new PaymentDetailRequest("NON_EXISTENT_PAYMENT");

        // When & Then
        mockMvc.perform(post("/api/shop/payment/ecpay/params")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/shop/payment/ecpay/params - 已付款的訂單仍可取得付款參數(用於查詢)")
    void testGetECPayParams_AlreadyPaid() throws Exception {
        // Given: 建立已付款的付款記錄
        PaymentEntity paidPayment = paymentTestFixtures.createPaidPayment(
                testOrder.getId(),
                2,
                BigDecimal.valueOf(1000),
                "Credit"
        );
        PaymentDetailRequest request = new PaymentDetailRequest(paidPayment.getPaymentNumber());

        // When & Then: 已付款的付款記錄仍可取得參數(業務邏輯允許)
        mockMvc.perform(post("/api/shop/payment/ecpay/params")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.params").exists());
    }
}
