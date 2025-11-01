package com.mimimart.domain.payment.model;

import com.mimimart.domain.order.model.Money;
import com.mimimart.domain.payment.exception.InvalidPaymentStatusException;
import com.mimimart.domain.payment.exception.PaymentAmountMismatchException;
import com.mimimart.infrastructure.persistence.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 付款領域模型 (富領域模型 - Aggregate Root)
 * 封裝付款相關業務邏輯
 *
 * 設計理念:
 * - 使用有業務語義的方法,而非 setter (如 markAsPaid() 而非 setStatus())
 * - 封裝狀態轉換規則與驗證邏輯
 * - 確保領域模型狀態永遠有效
 * - 透過值對象強化類型安全
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3)
 */
public class Payment {

    // === 識別與基本資料 ===
    private Long id;
    private Long orderId;
    private PaymentNumber paymentNumber;
    private String paymentMethod;
    private PaymentStatus status;

    // === 金額資訊 ===
    private Money amount;

    // === 第三方支付資訊 ===
    private String externalTransactionId;

    // === 時間戳記 ===
    private Instant expiredAt;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 私有建構函式,強制透過工廠方法建立
     */
    private Payment() {
    }

    // ========================================
    // 工廠方法 (Factory Methods)
    // ========================================

    /**
     * 工廠方法:建立新的付款記錄
     *
     * @param orderId 訂單 ID
     * @param amount 付款金額
     * @param paymentMethod 付款方式
     * @param expirationMinutes 付款期限(分鐘)
     * @return 新付款領域模型
     */
    public static Payment create(Long orderId,
                                  Money amount,
                                  String paymentMethod,
                                  int expirationMinutes) {
        Objects.requireNonNull(orderId, "訂單 ID 不能為 null");
        Objects.requireNonNull(amount, "付款金額不能為 null");
        Objects.requireNonNull(paymentMethod, "付款方式不能為 null");

        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("付款期限必須大於 0");
        }

        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paymentNumber = PaymentNumber.generate();
        payment.paymentMethod = paymentMethod;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING_PAYMENT;
        payment.expiredAt = Instant.now().plusSeconds(expirationMinutes * 60L);
        payment.createdAt = Instant.now();
        payment.updatedAt = Instant.now();

        return payment;
    }

    /**
     * 工廠方法:從持久化資料重建付款記錄
     *
     * @return 付款建構器
     */
    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    // ========================================
    // 業務方法 (Business Methods)
    // ========================================

    /**
     * 標記付款為已完成
     * 業務規則:只有待付款的記錄可以標記為已付款
     *
     * @param externalTransactionId 第三方交易 ID
     * @param callbackAmount 回調金額
     * @throws InvalidPaymentStatusException 若付款狀態不允許標記為已付款
     * @throws PaymentAmountMismatchException 若金額不符
     */
    public void markAsPaid(String externalTransactionId, BigDecimal callbackAmount) {
        // 驗證狀態
        if (this.status != PaymentStatus.PENDING_PAYMENT) {
            throw new InvalidPaymentStatusException("標記為已付款", this.status);
        }

        // 驗證金額
        if (this.amount.getAmount().compareTo(callbackAmount) != 0) {
            throw new PaymentAmountMismatchException(this.amount.getAmount(), callbackAmount);
        }

        this.status = PaymentStatus.PAID;
        this.externalTransactionId = externalTransactionId;
        this.paidAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 取消付款
     * 業務規則:只有待付款的記錄可以取消
     *
     * @throws InvalidPaymentStatusException 若付款狀態不允許取消
     */
    public void cancel() {
        if (this.status != PaymentStatus.PENDING_PAYMENT) {
            throw new InvalidPaymentStatusException("取消", this.status);
        }

        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * 檢查付款是否已完成
     *
     * @return true 若付款已完成
     */
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    /**
     * 檢查付款是否可以取消
     *
     * @return true 若付款可以取消
     */
    public boolean isCancellable() {
        return this.status == PaymentStatus.PENDING_PAYMENT;
    }

    /**
     * 檢查付款是否已過期
     *
     * @return true 若付款已過期
     */
    public boolean isExpired() {
        return this.expiredAt != null && Instant.now().isAfter(this.expiredAt);
    }

    // ========================================
    // Getters (查詢狀態,不提供 Setters)
    // ========================================

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PaymentNumber getPaymentNumber() {
        return paymentNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Money getAmount() {
        return amount;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ========================================
    // Builder (用於從持久化資料重建)
    // ========================================

    public static class PaymentBuilder {
        private final Payment payment = new Payment();

        public PaymentBuilder id(Long id) {
            payment.id = id;
            return this;
        }

        public PaymentBuilder orderId(Long orderId) {
            payment.orderId = orderId;
            return this;
        }

        public PaymentBuilder paymentNumber(PaymentNumber paymentNumber) {
            payment.paymentNumber = paymentNumber;
            return this;
        }

        public PaymentBuilder paymentMethod(String paymentMethod) {
            payment.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentBuilder status(PaymentStatus status) {
            payment.status = status;
            return this;
        }

        public PaymentBuilder amount(Money amount) {
            payment.amount = amount;
            return this;
        }

        public PaymentBuilder externalTransactionId(String externalTransactionId) {
            payment.externalTransactionId = externalTransactionId;
            return this;
        }

        public PaymentBuilder expiredAt(Instant expiredAt) {
            payment.expiredAt = expiredAt;
            return this;
        }

        public PaymentBuilder paidAt(Instant paidAt) {
            payment.paidAt = paidAt;
            return this;
        }

        public PaymentBuilder createdAt(Instant createdAt) {
            payment.createdAt = createdAt;
            return this;
        }

        public PaymentBuilder updatedAt(Instant updatedAt) {
            payment.updatedAt = updatedAt;
            return this;
        }

        public Payment build() {
            return payment;
        }
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", paymentNumber=" + paymentNumber +
                ", status=" + status +
                ", amount=" + amount +
                ", orderId=" + orderId +
                ", createdAt=" + createdAt +
                '}';
    }
}
