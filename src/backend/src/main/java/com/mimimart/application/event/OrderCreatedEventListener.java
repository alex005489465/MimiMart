package com.mimimart.application.event;

import com.mimimart.application.service.PaymentService;
import com.mimimart.domain.order.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 訂單建立事件監聽器
 * 當訂單建立後,自動建立對應的付款記錄
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (Payment Integration)
 */
@Component
public class OrderCreatedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventListener.class);

    private final PaymentService paymentService;

    public OrderCreatedEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 處理訂單建立事件
     * 在訂單事務提交後執行,確保訂單已成功儲存
     *
     * @param event 訂單建立事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("收到訂單建立事件: orderNumber={}, totalAmount={}",
                event.getOrderNumber(), event.getTotalAmount());

        try {
            // 建立付款記錄
            paymentService.createPayment(event.getOrderNumber(), event.getTotalAmount());

            logger.info("付款記錄建立成功: orderNumber={}", event.getOrderNumber());
        } catch (Exception e) {
            logger.error("建立付款記錄失敗: orderNumber={}, error={}",
                    event.getOrderNumber(), e.getMessage(), e);
            // 注意:此處異常不會回滾訂單事務(因為是 AFTER_COMMIT)
            // 可以考慮加入重試機制或補償邏輯
        }
    }
}
