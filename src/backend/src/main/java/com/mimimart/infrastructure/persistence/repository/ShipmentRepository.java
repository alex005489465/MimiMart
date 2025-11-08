package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 物流資料存取介面
 */
@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {
    /**
     * 根據訂單 ID 查詢物流資訊
     */
    Optional<ShipmentEntity> findByOrderId(Long orderId);

    /**
     * 根據物流追蹤號碼查詢
     */
    Optional<ShipmentEntity> findByTrackingNumber(String trackingNumber);

    /**
     * 檢查訂單是否已有物流記錄
     */
    boolean existsByOrderId(Long orderId);
}
