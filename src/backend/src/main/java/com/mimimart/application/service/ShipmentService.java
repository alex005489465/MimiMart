package com.mimimart.application.service;

import com.mimimart.domain.order.model.DeliveryInfo;
import com.mimimart.domain.shipment.exception.ShipmentNotFoundException;
import com.mimimart.domain.shipment.model.DeliveryStatus;
import com.mimimart.domain.shipment.model.Shipment;
import com.mimimart.domain.shipment.model.ShippingInfo;
import com.mimimart.infrastructure.persistence.entity.ShipmentEntity;
import com.mimimart.infrastructure.persistence.mapper.ShipmentMapper;
import com.mimimart.infrastructure.persistence.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流應用服務
 */
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    /**
     * 建立物流單（訂單建立時呼叫）
     *
     * @param orderId      訂單 ID
     * @param deliveryInfo 配送資訊（由會員填寫）
     * @param shippingFee  運費
     * @return Shipment ID
     */
    @Transactional
    public Long createShipment(Long orderId, DeliveryInfo deliveryInfo, BigDecimal shippingFee) {
        // 建立物流領域模型
        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .fromDeliveryInfo(deliveryInfo, shippingFee)
                .deliveryStatus(DeliveryStatus.PREPARING)
                .build();

        // 持久化
        ShipmentEntity entity = shipmentMapper.toEntity(shipment);
        ShipmentEntity savedEntity = shipmentRepository.save(entity);

        return savedEntity.getId();
    }

    /**
     * 記錄出貨資訊（後台管理員填寫物流商、追蹤號碼等）
     *
     * @param orderId      訂單 ID
     * @param shippingInfo 物流資訊
     */
    @Transactional
    public void recordShipping(Long orderId, ShippingInfo shippingInfo) {
        // 查詢物流記錄
        Shipment shipment = getShipmentByOrderId(orderId);

        // 記錄出貨資訊（領域邏輯會驗證狀態並更新）
        shipment.recordShipping(shippingInfo);

        // 持久化
        ShipmentEntity entity = shipmentMapper.toEntity(shipment);
        shipmentRepository.save(entity);
    }

    /**
     * 更新配送狀態
     *
     * @param orderId 訂單 ID
     * @param status  新狀態
     * @param notes   備註
     */
    @Transactional
    public void updateDeliveryStatus(Long orderId, DeliveryStatus status, String notes) {
        // 查詢物流記錄
        Shipment shipment = getShipmentByOrderId(orderId);

        // 更新狀態（領域邏輯會驗證狀態轉換是否合法）
        shipment.updateStatus(status, notes);

        // 持久化
        ShipmentEntity entity = shipmentMapper.toEntity(shipment);
        shipmentRepository.save(entity);
    }

    /**
     * 標記為已送達
     *
     * @param orderId             訂單 ID
     * @param actualDeliveryDate  實際送達時間
     */
    @Transactional
    public void markAsDelivered(Long orderId, LocalDateTime actualDeliveryDate) {
        // 查詢物流記錄
        Shipment shipment = getShipmentByOrderId(orderId);

        // 標記為已送達
        shipment.markAsDelivered(actualDeliveryDate);

        // 持久化
        ShipmentEntity entity = shipmentMapper.toEntity(shipment);
        shipmentRepository.save(entity);
    }

    /**
     * 根據訂單 ID 查詢物流資訊
     *
     * @param orderId 訂單 ID
     * @return 物流領域模型
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentByOrderId(Long orderId) {
        ShipmentEntity entity = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException(orderId));
        return shipmentMapper.toDomain(entity);
    }

    /**
     * 根據追蹤號碼查詢物流資訊
     *
     * @param trackingNumber 追蹤號碼
     * @return 物流領域模型
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        ShipmentEntity entity = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("找不到追蹤號碼為 " + trackingNumber + " 的物流資訊"));
        return shipmentMapper.toDomain(entity);
    }
}
