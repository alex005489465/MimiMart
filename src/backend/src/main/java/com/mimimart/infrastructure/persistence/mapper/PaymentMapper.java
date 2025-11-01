package com.mimimart.infrastructure.persistence.mapper;

import com.mimimart.domain.order.model.Money;
import com.mimimart.domain.payment.model.Payment;
import com.mimimart.domain.payment.model.PaymentNumber;
import com.mimimart.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

/**
 * 付款映射器 (基礎設施層)
 * 負責領域模型與 JPA 實體之間的轉換
 *
 * 設計理念:
 * - 隔離領域模型與持久化技術
 * - 領域模型不依賴 JPA 註解
 * - 基礎設施層負責資料轉換
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
@Component
public class PaymentMapper {

    /**
     * 將 JPA 實體轉換為領域模型
     *
     * @param paymentEntity 付款 JPA 實體
     * @return 付款領域模型
     */
    public Payment toDomain(PaymentEntity paymentEntity) {
        if (paymentEntity == null) {
            return null;
        }

        return Payment.builder()
                .id(paymentEntity.getId())
                .orderId(paymentEntity.getOrderId())
                .paymentNumber(PaymentNumber.of(paymentEntity.getPaymentNumber()))
                .paymentMethod(paymentEntity.getPaymentMethod())
                .status(paymentEntity.getStatus())
                .amount(Money.of(paymentEntity.getAmount()))
                .externalTransactionId(paymentEntity.getExternalTransactionId())
                .expiredAt(paymentEntity.getExpiredAt())
                .paidAt(paymentEntity.getPaidAt())
                .createdAt(paymentEntity.getCreatedAt())
                .updatedAt(paymentEntity.getUpdatedAt())
                .build();
    }

    /**
     * 將領域模型轉換為 JPA 實體
     *
     * @param payment 付款領域模型
     * @return 付款 JPA 實體
     */
    public PaymentEntity toEntity(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setPaymentNumber(payment.getPaymentNumber().getValue());
        entity.setPaymentMethod(payment.getPaymentMethod());
        entity.setStatus(payment.getStatus());
        entity.setAmount(payment.getAmount().getAmount());
        entity.setExternalTransactionId(payment.getExternalTransactionId());
        entity.setExpiredAt(payment.getExpiredAt());
        entity.setPaidAt(payment.getPaidAt());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());

        return entity;
    }
}
