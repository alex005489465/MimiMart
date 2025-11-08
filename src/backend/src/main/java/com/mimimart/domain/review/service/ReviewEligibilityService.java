package com.mimimart.domain.review.service;

import com.mimimart.domain.review.exception.ReviewAlreadyExistsException;
import com.mimimart.domain.review.exception.ReviewNotEligibleException;
import com.mimimart.domain.review.exception.UnauthorizedReviewException;
import com.mimimart.domain.shipment.exception.ShipmentNotFoundException;
import com.mimimart.domain.shipment.model.DeliveryStatus;
import com.mimimart.infrastructure.persistence.entity.OrderItemEntity;
import com.mimimart.infrastructure.persistence.entity.ShipmentEntity;
import com.mimimart.infrastructure.persistence.repository.ShipmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 評價資格驗證領域服務
 * 負責驗證會員是否有權對商品進行評價
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewEligibilityService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ShipmentRepository shipmentRepository;

    /**
     * 驗證評價資格的核心方法
     * 驗證項目：
     * 1. 訂單項目存在性
     * 2. 訂單擁有權（memberId 檢查）
     * 3. 配送狀態（必須為 DELIVERED）
     * 4. 評價期限（送達後 7 天內）
     * 5. 唯一性（尚未實作，在 ReviewService 層檢查）
     *
     * @param memberId 會員ID
     * @param orderItemId 訂單項目ID
     * @throws ReviewNotEligibleException 評價資格不符
     * @throws UnauthorizedReviewException 未經授權
     * @throws ShipmentNotFoundException 物流記錄不存在
     */
    public void validateReviewEligibility(Long memberId, Long orderItemId) {
        // 1. 查詢訂單項目（含關聯的訂單）
        OrderItemEntity orderItem = entityManager.find(OrderItemEntity.class, orderItemId);
        if (orderItem == null) {
            throw new ReviewNotEligibleException("訂單項目不存在: ID=" + orderItemId);
        }

        // 2. 驗證訂單擁有權
        Long orderMemberId = orderItem.getOrder().getMemberId();
        if (!orderMemberId.equals(memberId)) {
            throw new UnauthorizedReviewException(
                "無權評價此商品: 訂單不屬於該會員 (memberId=" + memberId + ")"
            );
        }

        // 3. 查詢物流資訊
        Long orderId = orderItem.getOrder().getId();
        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ShipmentNotFoundException(
                "物流記錄不存在: orderId=" + orderId
            ));

        // 4. 驗證配送狀態（必須為已送達）
        if (shipment.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw new ReviewNotEligibleException(
                "商品尚未送達，無法評價 (當前狀態: " +
                shipment.getDeliveryStatus().getDescription() + ")"
            );
        }

        // 5. 驗證送達時間
        LocalDateTime actualDeliveryDate = shipment.getActualDeliveryDate();
        if (actualDeliveryDate == null) {
            throw new ReviewNotEligibleException("配送時間未記錄，無法評價");
        }

        // 6. 驗證評價期限（送達後 7 天內）
        LocalDateTime deadline = actualDeliveryDate.plusDays(7);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(deadline)) {
            throw new ReviewNotEligibleException(
                String.format(
                    "評價期限已過 (送達時間: %s，評價期限: %s)",
                    actualDeliveryDate.toLocalDate(),
                    deadline.toLocalDate()
                )
            );
        }
    }

    /**
     * 檢查評價資格（不拋出異常，回傳布林值）
     * 用於前端檢查是否顯示評價按鈕
     *
     * @param memberId 會員ID
     * @param orderItemId 訂單項目ID
     * @return 是否符合評價資格
     */
    public boolean isEligibleForReview(Long memberId, Long orderItemId) {
        try {
            validateReviewEligibility(memberId, orderItemId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
