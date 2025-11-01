package com.mimimart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 付款記錄 JPA Entity
 * 對應資料表: payments
 * 零約束設計:資料完整性由應用程式層驗證
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_number", columnList = "payment_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_expired_at", columnList = "expired_at"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "payment_number", nullable = false, length = 50)
    private String paymentNumber;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Custom Constructor
    public PaymentEntity(Long orderId, String paymentNumber, String paymentMethod, BigDecimal amount, Instant expiredAt) {
        this.orderId = orderId;
        this.paymentNumber = paymentNumber;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING_PAYMENT;
        this.expiredAt = expiredAt;
    }
}
