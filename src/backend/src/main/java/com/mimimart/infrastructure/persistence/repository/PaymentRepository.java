package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.PaymentEntity;
import com.mimimart.infrastructure.persistence.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 付款記錄資料存取層
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    /**
     * 根據付款編號查詢
     */
    Optional<PaymentEntity> findByPaymentNumber(String paymentNumber);

    /**
     * 根據訂單ID查詢
     */
    List<PaymentEntity> findByOrderId(Long orderId);

    /**
     * 根據訂單ID和狀態查詢
     */
    Optional<PaymentEntity> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

    /**
     * 查詢逾期未付款的記錄
     */
    List<PaymentEntity> findByStatusAndExpiredAtBefore(PaymentStatus status, Instant expiredAt);
}
