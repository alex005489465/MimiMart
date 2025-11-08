package com.mimimart.domain.order.service;

import com.mimimart.api.dto.order.CreateOrderRequest;
import com.mimimart.domain.order.exception.EmptyCartException;
import com.mimimart.domain.order.model.*;
import com.mimimart.domain.product.exception.ProductNotFoundException;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 訂單工廠(領域服務)
 * 負責從前端傳入的項目列表建立訂單
 */
@Service
@RequiredArgsConstructor
public class OrderFactory {

    private final ProductRepository productRepository;

    /**
     * 從項目列表建立訂單
     *
     * @param memberId     會員 ID
     * @param items        訂單項目列表
     * @param deliveryInfo 送貨資訊
     * @return Order 訂單(領域模型)
     * @throws EmptyCartException 項目列表為空時拋出
     */
    public Order createFromItems(Long memberId, List<CreateOrderRequest.OrderItemRequest> items, DeliveryInfo deliveryInfo) {
        // 驗證項目列表不為空
        if (items == null || items.isEmpty()) {
            throw new EmptyCartException();
        }

        // 批次查詢所有商品的最新資訊
        List<Long> productIds = items.stream()
                .map(CreateOrderRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 建立訂單項目(使用商品快照)
        List<OrderItem> orderItems = items.stream()
                .map(item -> createOrderItem(item, productMap))
                .collect(Collectors.toList());

        // 計算訂單總金額
        Money totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);

        // 生成訂單編號
        OrderNumber orderNumber = OrderNumber.generate();

        // 建立訂單
        return Order.builder()
                .memberId(memberId)
                .orderNumber(orderNumber)
                .status(OrderStatus.PAYMENT_PENDING)
                .items(orderItems)
                .totalAmount(totalAmount)
                .deliveryInfo(deliveryInfo)
                .build();
    }

    /**
     * 從請求項目建立訂單項目
     * 快照當下的商品資訊,不受後續商品價格變動影響
     */
    private OrderItem createOrderItem(CreateOrderRequest.OrderItemRequest item, Map<Long, Product> productMap) {
        Product product = productMap.get(item.getProductId());

        if (product == null) {
            throw new ProductNotFoundException(item.getProductId());
        }

        // 建立商品快照
        OrderItem.ProductSnapshot snapshot = OrderItem.ProductSnapshot.builder()
                .productName(product.getName())
                .price(Money.of(product.getPrice()))
                .productImage(product.getImageUrl())
                .build();

        // 建立訂單項目
        return OrderItem.of(
                item.getProductId(),
                snapshot,
                item.getQuantity()
        );
    }
}
