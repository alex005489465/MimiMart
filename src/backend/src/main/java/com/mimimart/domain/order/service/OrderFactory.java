package com.mimimart.domain.order.service;

import com.mimimart.domain.cart.model.Cart;
import com.mimimart.domain.cart.model.CartItem;
import com.mimimart.domain.order.exception.EmptyCartException;
import com.mimimart.domain.order.model.*;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 訂單工廠(領域服務)
 * 負責從購物車建立訂單的複雜邏輯
 */
@Service
public class OrderFactory {

    private final ProductRepository productRepository;

    public OrderFactory(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 從購物車建立訂單
     *
     * @param cart         購物車(領域模型)
     * @param deliveryInfo 送貨資訊
     * @return Order 訂單(領域模型)
     * @throws EmptyCartException 購物車為空時拋出
     */
    public Order createFromCart(Cart cart, DeliveryInfo deliveryInfo) {
        // 驗證購物車不為空
        if (cart.isEmpty()) {
            throw new EmptyCartException();
        }

        // 批次查詢購物車中所有商品的最新資訊
        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 建立訂單項目(使用商品快照)
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> createOrderItem(cartItem, productMap))
                .collect(Collectors.toList());

        // 計算訂單總金額
        Money totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);

        // 生成訂單編號
        OrderNumber orderNumber = OrderNumber.generate();

        // 建立訂單
        return Order.builder()
                .memberId(cart.getMemberId())
                .orderNumber(orderNumber)
                .status(OrderStatus.PAYMENT_PENDING)
                .items(orderItems)
                .totalAmount(totalAmount)
                .deliveryInfo(deliveryInfo)
                .build();
    }

    /**
     * 從購物車項目建立訂單項目
     * 快照當下的商品資訊,不受後續商品價格變動影響
     */
    private OrderItem createOrderItem(CartItem cartItem, Map<Long, Product> productMap) {
        Product product = productMap.get(cartItem.getProductId());

        if (product == null) {
            throw new IllegalStateException("商品不存在: " + cartItem.getProductId());
        }

        // 建立商品快照
        OrderItem.ProductSnapshot snapshot = OrderItem.ProductSnapshot.builder()
                .productName(product.getName())
                .price(Money.of(product.getPrice()))
                .originalPrice(Money.of(product.getOriginalPrice()))
                .productImage(product.getImageUrl())
                .build();

        // 建立訂單項目
        return OrderItem.of(
                cartItem.getProductId(),
                snapshot,
                cartItem.getQuantity()
        );
    }
}
