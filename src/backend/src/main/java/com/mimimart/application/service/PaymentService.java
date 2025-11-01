package com.mimimart.application.service;

import com.mimimart.domain.order.model.Money;
import com.mimimart.domain.order.model.Order;
import com.mimimart.domain.payment.exception.PaymentNotFoundException;
import com.mimimart.domain.payment.model.Payment;
import com.mimimart.infrastructure.payment.ecpay.ECPayService;
import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import com.mimimart.infrastructure.persistence.entity.PaymentEntity;
import com.mimimart.infrastructure.persistence.entity.PaymentStatus;
import com.mimimart.infrastructure.persistence.mapper.OrderMapper;
import com.mimimart.infrastructure.persistence.mapper.PaymentMapper;
import com.mimimart.infrastructure.persistence.repository.OrderRepository;
import com.mimimart.infrastructure.persistence.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * 付款服務 (應用層)
 * 協調領域模型與基礎設施層
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final ECPayService ecPayService;

    @Value("${mimimart.payment.expiration-minutes:30}")
    private int expirationMinutes;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          PaymentMapper paymentMapper,
                          OrderMapper orderMapper,
                          ECPayService ecPayService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.ecPayService = ecPayService;
    }

    /**
     * 建立付款記錄(由訂單建立事件觸發)
     *
     * @param orderNumber 訂單編號
     * @param totalAmount 訂單總金額
     * @return 付款領域模型
     */
    @Transactional
    public Payment createPayment(String orderNumber, BigDecimal totalAmount) {
        logger.info("建立付款記錄: orderNumber={}, amount={}", orderNumber, totalAmount);

        // 1. 查詢訂單
        OrderEntity orderEntity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("訂單不存在: " + orderNumber));

        // 2. 檢查是否已有待付款記錄(重複付款防護)
        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderIdAndStatus(
                orderEntity.getId(), PaymentStatus.PENDING_PAYMENT);
        if (existingPayment.isPresent()) {
            logger.warn("訂單已有待付款記錄: orderNumber={}", orderNumber);
            return paymentMapper.toDomain(existingPayment.get());
        }

        // 3. 使用領域模型建立付款記錄
        Payment payment = Payment.create(
                orderEntity.getId(),
                Money.of(totalAmount),
                "ECPAY_Credit", // 預設使用綠界信用卡
                expirationMinutes
        );

        // 4. 將領域模型轉換為 JPA 實體並儲存
        PaymentEntity paymentEntity = paymentMapper.toEntity(payment);
        paymentEntity = paymentRepository.save(paymentEntity);

        // 5. 更新領域模型 ID(儲存後才有 ID)
        payment = paymentMapper.toDomain(paymentEntity);

        logger.info("付款記錄建立成功: paymentNumber={}, expiredAt={}",
                payment.getPaymentNumber().getValue(), payment.getExpiredAt());

        return payment;
    }

    /**
     * 查詢付款詳情
     *
     * @param paymentNumber 付款編號
     * @return 付款領域模型
     */
    public Payment getPaymentDetail(String paymentNumber) {
        PaymentEntity paymentEntity = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new PaymentNotFoundException(paymentNumber));

        return paymentMapper.toDomain(paymentEntity);
    }

    /**
     * 取得付款的綠界參數
     *
     * @param paymentNumber 付款編號
     * @return 綠界 API 參數
     */
    public Map<String, String> getECPayParams(String paymentNumber) {
        Payment payment = getPaymentDetail(paymentNumber);

        // 取得訂單資訊作為商品描述
        OrderEntity orderEntity = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        String itemDescription = "訂單 " + orderEntity.getOrderNumber();

        return ecPayService.createPaymentParams(payment, itemDescription, "Credit");
    }

    /**
     * 處理付款回調(綠界通知)
     *
     * @param callbackParams 綠界回調參數
     */
    @Transactional
    public void handlePaymentCallback(Map<String, String> callbackParams) {
        // 1. 驗證簽名
        if (!ecPayService.verifyCallback(callbackParams)) {
            logger.error("簽名驗證失敗: {}", callbackParams);
            throw new RuntimeException("簽名驗證失敗");
        }

        // 2. 解析回調參數
        ECPayService.ECPayCallbackInfo callbackInfo = ecPayService.parseCallback(callbackParams);

        logger.info("收到付款回調: merchantTradeNo={}, tradeNo={}, rtnCode={}",
                callbackInfo.getMerchantTradeNo(), callbackInfo.getTradeNo(), callbackInfo.getRtnCode());

        // 3. 檢查付款是否成功
        if (!ecPayService.isPaymentSuccess(callbackInfo.getRtnCode())) {
            logger.warn("付款失敗: merchantTradeNo={}, rtnMsg={}",
                    callbackInfo.getMerchantTradeNo(), callbackInfo.getRtnMsg());
            return;
        }

        // 4. 查詢付款記錄
        PaymentEntity paymentEntity = paymentRepository
                .findByPaymentNumber(callbackInfo.getMerchantTradeNo())
                .orElseThrow(() -> new PaymentNotFoundException(callbackInfo.getMerchantTradeNo()));

        // 5. 轉換為領域模型
        Payment payment = paymentMapper.toDomain(paymentEntity);

        // 6. 冪等性檢查 - 如果已付款則直接返回
        if (payment.isPaid()) {
            logger.info("付款記錄已處理,忽略重複回調: paymentNumber={}", callbackInfo.getMerchantTradeNo());
            return;
        }

        // 7. 執行領域邏輯(包含狀態驗證與金額驗證)
        BigDecimal callbackAmount = new BigDecimal(callbackInfo.getTradeAmt());
        payment.markAsPaid(callbackInfo.getTradeNo(), callbackAmount);

        // 8. 將領域模型轉換回 JPA 實體並儲存
        paymentEntity = paymentMapper.toEntity(payment);
        paymentRepository.save(paymentEntity);

        // 9. 更新訂單狀態
        OrderEntity orderEntity = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        Order order = orderMapper.toDomain(orderEntity);
        order.markAsPaid();

        orderEntity = orderMapper.toEntity(order);
        orderRepository.save(orderEntity);

        logger.info("付款處理完成: paymentNumber={}, orderId={}",
                payment.getPaymentNumber().getValue(), orderEntity.getId());
    }

    /**
     * 取消付款
     *
     * @param orderId 訂單 ID
     */
    @Transactional
    public void cancelPayment(Long orderId) {
        Optional<PaymentEntity> paymentEntityOpt =
                paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING_PAYMENT);

        if (paymentEntityOpt.isPresent()) {
            // 轉換為領域模型
            Payment payment = paymentMapper.toDomain(paymentEntityOpt.get());

            // 執行領域邏輯
            payment.cancel();

            // 將領域模型轉換回 JPA 實體並儲存
            PaymentEntity paymentEntity = paymentMapper.toEntity(payment);
            paymentRepository.save(paymentEntity);

            logger.info("付款已取消: paymentNumber={}, orderId={}",
                    payment.getPaymentNumber().getValue(), orderId);
        }
    }
}
