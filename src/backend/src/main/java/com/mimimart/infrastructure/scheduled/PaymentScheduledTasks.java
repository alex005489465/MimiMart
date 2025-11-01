package com.mimimart.infrastructure.scheduled;

import com.mimimart.domain.order.model.Order;
import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import com.mimimart.infrastructure.persistence.entity.PaymentEntity;
import com.mimimart.infrastructure.persistence.entity.PaymentStatus;
import com.mimimart.infrastructure.persistence.mapper.OrderMapper;
import com.mimimart.infrastructure.persistence.repository.OrderRepository;
import com.mimimart.infrastructure.persistence.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 付款定時任務
 * 自動處理逾期未付款的訂單
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Component
public class PaymentScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduledTasks.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public PaymentScheduledTasks(PaymentRepository paymentRepository,
                                 OrderRepository orderRepository,
                                 OrderMapper orderMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    /**
     * 處理逾期付款
     * 每 5 分鐘執行一次
     */
    @Scheduled(cron = "${mimimart.payment.expired-check-cron:0 */5 * * * ?}")
    @Transactional
    public void handleExpiredPayments() {
        logger.info("開始執行逾期付款檢查");

        try {
            // 查詢所有逾期的待付款記錄
            List<PaymentEntity> expiredPayments = paymentRepository.findByStatusAndExpiredAtBefore(
                    PaymentStatus.PENDING_PAYMENT,
                    Instant.now()
            );

            if (expiredPayments.isEmpty()) {
                logger.info("沒有逾期的付款記錄");
                return;
            }

            logger.info("找到 {} 筆逾期付款記錄", expiredPayments.size());

            int successCount = 0;
            int failureCount = 0;

            for (PaymentEntity payment : expiredPayments) {
                try {
                    // 更新付款狀態為逾期
                    payment.setStatus(PaymentStatus.EXPIRED);
                    payment.setUpdatedAt(Instant.now());
                    paymentRepository.save(payment);

                    // 更新訂單狀態為已取消
                    OrderEntity orderEntity = orderRepository.findById(payment.getOrderId()).orElse(null);
                    if (orderEntity != null && orderEntity.getStatus().isCancellable()) {
                        // 轉換為領域模型並執行業務邏輯
                        Order order = orderMapper.toDomain(orderEntity);
                        order.cancel("付款逾期,系統自動取消");

                        // 轉換回實體並儲存
                        orderEntity = orderMapper.toEntity(order);
                        orderRepository.save(orderEntity);

                        logger.info("逾期付款處理成功: paymentNumber={}, orderId={}",
                                payment.getPaymentNumber(), orderEntity.getId());
                        successCount++;
                    } else {
                        logger.warn("訂單狀態異常,無法取消: orderId={}, status={}",
                                payment.getOrderId(), orderEntity != null ? orderEntity.getStatus() : "NULL");
                        failureCount++;
                    }

                } catch (Exception e) {
                    logger.error("處理逾期付款失敗: paymentNumber={}, error={}",
                            payment.getPaymentNumber(), e.getMessage(), e);
                    failureCount++;
                }
            }

            logger.info("逾期付款檢查完成: 成功={}, 失敗={}", successCount, failureCount);

        } catch (Exception e) {
            logger.error("逾期付款檢查發生異常", e);
        }
    }
}
