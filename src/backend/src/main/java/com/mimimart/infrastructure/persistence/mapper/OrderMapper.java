package com.mimimart.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.domain.order.model.*;
import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import com.mimimart.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 訂單 Mapper
 * 負責領域模型 ↔ JPA 實體的雙向轉換
 */
@Component
public class OrderMapper {

    private final ObjectMapper objectMapper;

    public OrderMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 領域模型 → JPA 實體
     */
    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setMemberId(order.getMemberId());
        entity.setOrderNumber(order.getOrderNumber().getValue());
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setDeliveryInfo(serializeDeliveryInfo(order.getDeliveryInfo()));
        entity.setCancellationReason(order.getCancellationReason());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        // 轉換訂單項目
        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .collect(Collectors.toList());
        entity.setItems(itemEntities);

        return entity;
    }

    /**
     * JPA 實體 → 領域模型
     */
    public Order toDomain(OrderEntity entity) {
        // 轉換訂單項目
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());

        return Order.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .orderNumber(OrderNumber.of(entity.getOrderNumber()))
                .status(entity.getStatus())
                .items(items)
                .totalAmount(Money.of(entity.getTotalAmount()))
                .deliveryInfo(deserializeDeliveryInfo(entity.getDeliveryInfo()))
                .cancellationReason(entity.getCancellationReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 訂單項目:領域模型 → JPA 實體
     */
    private OrderItemEntity toItemEntity(OrderItem item, OrderEntity orderEntity) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setOrder(orderEntity);
        entity.setProductId(item.getProductId());
        entity.setSnapshotData(serializeSnapshot(item.getSnapshot()));
        entity.setQuantity(item.getQuantity());
        entity.setSubtotal(item.getSubtotal().getAmount());
        return entity;
    }

    /**
     * 訂單項目:JPA 實體 → 領域模型
     */
    private OrderItem toItemDomain(OrderItemEntity entity) {
        OrderItem.ProductSnapshot snapshot = deserializeSnapshot(entity.getSnapshotData());
        return OrderItem.of(
                entity.getProductId(),
                snapshot,
                entity.getQuantity()
        );
    }

    /**
     * 序列化送貨資訊為 JSON
     */
    private String serializeDeliveryInfo(DeliveryInfo deliveryInfo) {
        try {
            Map<String, String> data = Map.of(
                    "receiverName", deliveryInfo.getReceiverName(),
                    "receiverPhone", deliveryInfo.getReceiverPhone(),
                    "shippingAddress", deliveryInfo.getShippingAddress(),
                    "deliveryMethod", deliveryInfo.getDeliveryMethod().name(),
                    "deliveryNote", deliveryInfo.getDeliveryNote() != null ? deliveryInfo.getDeliveryNote() : ""
            );
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("送貨資訊序列化失敗", e);
        }
    }

    /**
     * 反序列化送貨資訊
     */
    private DeliveryInfo deserializeDeliveryInfo(String json) {
        try {
            Map<String, String> data = objectMapper.readValue(json, new TypeReference<>() {});
            return DeliveryInfo.builder()
                    .receiverName(data.get("receiverName"))
                    .receiverPhone(data.get("receiverPhone"))
                    .shippingAddress(data.get("shippingAddress"))
                    .deliveryMethod(DeliveryInfo.DeliveryMethod.valueOf(data.get("deliveryMethod")))
                    .deliveryNote(data.get("deliveryNote"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("送貨資訊反序列化失敗", e);
        }
    }

    /**
     * 序列化商品快照為 JSON
     */
    private String serializeSnapshot(OrderItem.ProductSnapshot snapshot) {
        try {
            Map<String, Object> data = Map.of(
                    "productName", snapshot.getProductName(),
                    "productPrice", snapshot.getPrice().getAmount(),
                    "productImage", snapshot.getProductImage() != null ? snapshot.getProductImage() : ""
            );
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("商品快照序列化失敗", e);
        }
    }

    /**
     * 反序列化商品快照
     */
    private OrderItem.ProductSnapshot deserializeSnapshot(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});
            return OrderItem.ProductSnapshot.builder()
                    .productName((String) data.get("productName"))
                    .price(Money.of(new BigDecimal(data.get("productPrice").toString())))
                    .productImage((String) data.get("productImage"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("商品快照反序列化失敗", e);
        }
    }
}
