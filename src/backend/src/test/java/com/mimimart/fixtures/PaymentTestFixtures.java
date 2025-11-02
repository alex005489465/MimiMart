package com.mimimart.fixtures;

import com.mimimart.domain.order.model.OrderStatus;
import com.mimimart.infrastructure.payment.ecpay.ECPayEncryption;
import com.mimimart.infrastructure.persistence.entity.*;
import com.mimimart.infrastructure.persistence.repository.OrderRepository;
import com.mimimart.infrastructure.persistence.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 付款測試資料輔助類
 * 提供綠界付款測試所需的測試資料建立方法
 *
 * 命名策略:
 * - 訂單: 測試訂單-{timestamp}-{index}
 * - 付款: PAY{timestamp}{index}
 *
 * 特點:
 * - 使用時間戳確保每次測試資料唯一
 * - 透過 @Transactional 測試完自動回滾
 * - 提供模擬綠界回調參數的方法
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@Component
public class PaymentTestFixtures {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ECPayEncryption ecPayEncryption;

    /**
     * 建立測試訂單(含訂單項目)
     *
     * @param memberId 會員 ID
     * @param index 訂單編號
     * @param totalAmount 訂單總金額
     * @return 測試訂單實體
     */
    public OrderEntity createTestOrder(Long memberId, int index, BigDecimal totalAmount) {
        long timestamp = System.currentTimeMillis();
        String orderNumber = String.format("ORD%d%03d", timestamp, index);

        OrderEntity order = new OrderEntity();
        order.setMemberId(memberId);
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(totalAmount);
        order.setDeliveryInfo("{\"receiverName\":\"測試收件人\",\"receiverPhone\":\"0912345678\",\"shippingAddress\":\"台北市測試路1號\",\"deliveryMethod\":\"HOME_DELIVERY\",\"deliveryNote\":\"\"}");

        // 儲存訂單(先不設置項目)
        order = orderRepository.save(order);

        // 創建訂單項目(測試用簡化版本)
        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(order);
        item.setProductId(1L); // 測試用商品 ID
        item.setQuantity(1);
        item.setSubtotal(totalAmount);
        item.setSnapshotData("{\"productName\":\"測試商品\",\"productPrice\":" + totalAmount + ",\"productOriginalPrice\":" + totalAmount + ",\"productImage\":\"\"}");

        // 將項目添加到訂單
        order.addItem(item);

        return orderRepository.save(order);
    }

    /**
     * 建立測試付款記錄
     *
     * @param orderId 訂單 ID
     * @param index 付款編號
     * @param amount 付款金額
     * @param paymentMethod 付款方式
     * @return 測試付款實體
     */
    public PaymentEntity createTestPayment(Long orderId, int index, BigDecimal amount, String paymentMethod) {
        long timestamp = System.currentTimeMillis();
        String paymentNumber = String.format("PAY%d%03d", timestamp, index);
        Instant expiredAt = Instant.now().plusSeconds(3600); // 1小時後過期

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(orderId);
        payment.setPaymentNumber(paymentNumber);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING_PAYMENT);
        payment.setExpiredAt(expiredAt);
        payment.setUpdatedAt(Instant.now());

        return paymentRepository.save(payment);
    }

    /**
     * 建立已付款的測試付款記錄
     *
     * @param orderId 訂單 ID
     * @param index 付款編號
     * @param amount 付款金額
     * @param paymentMethod 付款方式
     * @return 已付款的測試付款實體
     */
    public PaymentEntity createPaidPayment(Long orderId, int index, BigDecimal amount, String paymentMethod) {
        PaymentEntity payment = createTestPayment(orderId, index, amount, paymentMethod);
        payment.setStatus(PaymentStatus.PAID);
        payment.setExternalTransactionId("TEST_TXN_" + System.currentTimeMillis());
        payment.setPaidAt(Instant.now());
        return paymentRepository.save(payment);
    }

    /**
     * 模擬綠界付款成功回調參數
     *
     * @param merchantId 特店編號
     * @param paymentNumber 付款編號
     * @param amount 金額
     * @param hashKey 金鑰 HashKey
     * @param hashIv 金鑰 HashIV
     * @return 綠界回調參數 Map
     */
    public Map<String, String> createMockECPaySuccessCallback(
            String merchantId,
            String paymentNumber,
            BigDecimal amount,
            String hashKey,
            String hashIv) {

        String currentTime = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now());

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", paymentNumber);
        params.put("MerchantTradeDate", currentTime);
        params.put("PaymentType", "Credit_CreditCard");
        params.put("TradeNo", "TEST_ECPAY_TXN_" + System.currentTimeMillis());
        params.put("TradeAmt", String.valueOf(amount.intValue()));
        params.put("PaymentDate", currentTime);
        params.put("RtnCode", "1"); // 1 表示付款成功
        params.put("RtnMsg", "交易成功");
        params.put("TradeDate", currentTime);
        params.put("SimulatePaid", "0");
        params.put("PaymentTypeChargeFee", "0");
        params.put("CustomField1", "");
        params.put("CustomField2", "");
        params.put("CustomField3", "");
        params.put("CustomField4", "");

        // 產生檢查碼
        String checkMacValue = ecPayEncryption.generateCheckMacValue(params, hashKey, hashIv);
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    /**
     * 模擬綠界付款失敗回調參數
     *
     * @param merchantId 特店編號
     * @param paymentNumber 付款編號
     * @param amount 金額
     * @param hashKey 金鑰 HashKey
     * @param hashIv 金鑰 HashIV
     * @return 綠界回調參數 Map
     */
    public Map<String, String> createMockECPayFailedCallback(
            String merchantId,
            String paymentNumber,
            BigDecimal amount,
            String hashKey,
            String hashIv) {

        String currentTime = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now());

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", paymentNumber);
        params.put("MerchantTradeDate", currentTime);
        params.put("PaymentType", "Credit_CreditCard");
        params.put("TradeNo", "TEST_ECPAY_TXN_" + System.currentTimeMillis());
        params.put("TradeAmt", String.valueOf(amount.intValue()));
        params.put("PaymentDate", currentTime);
        params.put("RtnCode", "0"); // 0 表示付款失敗
        params.put("RtnMsg", "付款失敗");
        params.put("TradeDate", currentTime);
        params.put("SimulatePaid", "0");
        params.put("PaymentTypeChargeFee", "0");
        params.put("CustomField1", "");
        params.put("CustomField2", "");
        params.put("CustomField3", "");
        params.put("CustomField4", "");

        // 產生檢查碼
        String checkMacValue = ecPayEncryption.generateCheckMacValue(params, hashKey, hashIv);
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    /**
     * 模擬綠界付款返回參數(ClientBackURL)
     *
     * @param merchantId 特店編號
     * @param paymentNumber 付款編號
     * @param amount 金額
     * @param hashKey 金鑰 HashKey
     * @param hashIv 金鑰 HashIV
     * @return 綠界返回參數 Map
     */
    public Map<String, String> createMockECPayReturnParams(
            String merchantId,
            String paymentNumber,
            BigDecimal amount,
            String hashKey,
            String hashIv) {

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", paymentNumber);
        params.put("TradeNo", "TEST_ECPAY_TXN_" + System.currentTimeMillis());
        params.put("TradeAmt", String.valueOf(amount.intValue()));
        params.put("PaymentDate", java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now()));
        params.put("PaymentType", "Credit_CreditCard");
        params.put("RtnCode", "1");
        params.put("RtnMsg", "交易成功");
        params.put("TradeDate", java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now()));

        // 產生檢查碼
        String checkMacValue = ecPayEncryption.generateCheckMacValue(params, hashKey, hashIv);
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    /**
     * 建立錯誤的檢查碼回調參數(用於測試簽名驗證失敗)
     *
     * @param merchantId 特店編號
     * @param paymentNumber 付款編號
     * @param amount 金額
     * @return 包含錯誤檢查碼的回調參數 Map
     */
    public Map<String, String> createInvalidCheckMacValueCallback(
            String merchantId,
            String paymentNumber,
            BigDecimal amount) {

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", paymentNumber);
        params.put("TradeNo", "TEST_ECPAY_TXN_" + System.currentTimeMillis());
        params.put("TradeAmt", String.valueOf(amount.intValue()));
        params.put("PaymentDate", java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now()));
        params.put("PaymentType", "Credit_CreditCard");
        params.put("RtnCode", "1");
        params.put("RtnMsg", "交易成功");
        params.put("TradeDate", java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now()));
        params.put("SimulatePaid", "0");
        params.put("CheckMacValue", "INVALID_CHECK_MAC_VALUE"); // 錯誤的檢查碼

        return params;
    }
}
