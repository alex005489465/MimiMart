package com.mimimart.infrastructure.persistence.mapper;

import com.mimimart.domain.shipment.model.Shipment;
import com.mimimart.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.stereotype.Component;

/**
 * 物流領域模型與實體轉換器
 */
@Component
public class ShipmentMapper {
    /**
     * 領域模型轉實體
     */
    public ShipmentEntity toEntity(Shipment shipment) {
        if (shipment == null) {
            return null;
        }

        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(shipment.getId());
        entity.setOrderId(shipment.getOrderId());
        entity.setReceiverName(shipment.getReceiverName());
        entity.setReceiverPhone(shipment.getReceiverPhone());
        entity.setShippingAddress(shipment.getShippingAddress());
        entity.setDeliveryMethod(shipment.getDeliveryMethod());
        entity.setDeliveryNote(shipment.getDeliveryNote());
        entity.setShippingFee(shipment.getShippingFee());
        entity.setCarrier(shipment.getCarrier());
        entity.setTrackingNumber(shipment.getTrackingNumber());
        entity.setShippedAt(shipment.getShippedAt());
        entity.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        entity.setDeliveryStatus(shipment.getDeliveryStatus());
        entity.setActualDeliveryDate(shipment.getActualDeliveryDate());
        entity.setNotes(shipment.getNotes());
        entity.setCreatedAt(shipment.getCreatedAt());
        entity.setUpdatedAt(shipment.getUpdatedAt());

        return entity;
    }

    /**
     * 實體轉領域模型
     */
    public Shipment toDomain(ShipmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Shipment.builder()
            .id(entity.getId())
            .orderId(entity.getOrderId())
            .receiverName(entity.getReceiverName())
            .receiverPhone(entity.getReceiverPhone())
            .shippingAddress(entity.getShippingAddress())
            .deliveryMethod(entity.getDeliveryMethod())
            .deliveryNote(entity.getDeliveryNote())
            .shippingFee(entity.getShippingFee())
            .carrier(entity.getCarrier())
            .trackingNumber(entity.getTrackingNumber())
            .shippedAt(entity.getShippedAt())
            .estimatedDeliveryDate(entity.getEstimatedDeliveryDate())
            .deliveryStatus(entity.getDeliveryStatus())
            .actualDeliveryDate(entity.getActualDeliveryDate())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
