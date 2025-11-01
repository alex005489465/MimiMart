package com.mimimart.application.service;

import com.mimimart.domain.cart.model.Cart;
import com.mimimart.domain.order.event.OrderCreatedEvent;
import com.mimimart.domain.order.exception.OrderNotFoundException;
import com.mimimart.domain.order.exception.UnauthorizedOrderAccessException;
import com.mimimart.domain.order.model.*;
import com.mimimart.domain.order.service.OrderFactory;
import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import com.mimimart.infrastructure.persistence.mapper.CartMapper;
import com.mimimart.infrastructure.persistence.mapper.OrderMapper;
import com.mimimart.infrastructure.persistence.repository.CartItemRepository;
import com.mimimart.infrastructure.persistence.repository.OrderRepository;
import com.mimimart.infrastructure.persistence.repository.OrderSpecification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 訂單應用服務
 * 協調領域模型、基礎設施層,並發布領域事件
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderFactory orderFactory;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderFactory orderFactory,
            CartService cartService,
            CartItemRepository cartItemRepository,
            CartMapper cartMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderFactory = orderFactory;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.cartMapper = cartMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 前台:從購物車建立訂單
     *
     * @param memberId     會員 ID
     * @param deliveryInfo 送貨資訊
     * @return 訂單(領域模型)
     */
    @Transactional
    public Order createOrder(Long memberId, DeliveryInfo deliveryInfo) {
        // 1. 取得會員購物車(轉換為領域模型)
        List<com.mimimart.infrastructure.persistence.entity.CartItem> cartItemEntities =
                cartItemRepository.findByMemberId(memberId);
        Cart cart = cartMapper.toDomain(cartItemEntities, memberId);

        // 2. 使用領域服務建立訂單
        Order order = orderFactory.createFromCart(cart, deliveryInfo);

        // 3. 持久化訂單
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity savedEntity = orderRepository.save(entity);

        // 4. 清空購物車
        cartService.clearCart(memberId);

        // 5. 發布訂單建立事件(觸發付款流程)
        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getOrderNumber().getValue(),
                order.getMemberId(),
                order.getTotalAmount().getAmount()
        ));

        // 6. 返回領域模型
        return orderMapper.toDomain(savedEntity);
    }

    /**
     * 前台:查詢會員訂單列表
     *
     * @param memberId 會員 ID
     * @return 訂單列表
     */
    @Transactional(readOnly = true)
    public List<Order> getMemberOrders(Long memberId) {
        List<OrderEntity> entities = orderRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        return entities.stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 前台:查詢訂單詳情
     *
     * @param memberId    會員 ID
     * @param orderNumber 訂單編號
     * @return 訂單(領域模型)
     * @throws OrderNotFoundException              訂單不存在
     * @throws UnauthorizedOrderAccessException 無權存取
     */
    @Transactional(readOnly = true)
    public Order getOrderDetail(Long memberId, String orderNumber) {
        OrderEntity entity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        Order order = orderMapper.toDomain(entity);

        // 驗證權限
        if (!order.belongsToMember(memberId)) {
            throw new UnauthorizedOrderAccessException(orderNumber);
        }

        return order;
    }

    /**
     * 前台:取消訂單
     *
     * @param memberId    會員 ID
     * @param orderNumber 訂單編號
     * @throws OrderNotFoundException              訂單不存在
     * @throws UnauthorizedOrderAccessException 無權存取
     */
    @Transactional
    public void cancelOrder(Long memberId, String orderNumber) {
        OrderEntity entity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        Order order = orderMapper.toDomain(entity);

        // 驗證權限
        if (!order.belongsToMember(memberId)) {
            throw new UnauthorizedOrderAccessException(orderNumber);
        }

        // 領域模型處理取消邏輯(會驗證狀態)
        order.cancel("會員自行取消");

        // 更新實體
        entity.setStatus(order.getStatus());
        entity.setCancellationReason(order.getCancellationReason());
        entity.setUpdatedAt(order.getUpdatedAt());

        orderRepository.save(entity);
    }

    /**
     * 後台:查詢所有訂單(分頁+篩選)
     *
     * @param status      訂單狀態(可選)
     * @param orderNumber 訂單編號(模糊搜尋,可選)
     * @param startDate   開始日期(可選)
     * @param endDate     結束日期(可選)
     * @param pageable    分頁參數
     * @return 訂單分頁
     */
    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersAdmin(
            OrderStatus status,
            String orderNumber,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Specification<OrderEntity> spec = OrderSpecification.withFilters(
                status, orderNumber, startDate, endDate
        );

        Page<OrderEntity> entityPage = orderRepository.findAll(spec, pageable);
        return entityPage.map(orderMapper::toDomain);
    }

    /**
     * 後台:查詢訂單詳情
     *
     * @param orderNumber 訂單編號
     * @return 訂單(領域模型)
     * @throws OrderNotFoundException 訂單不存在
     */
    @Transactional(readOnly = true)
    public Order getOrderDetailAdmin(String orderNumber) {
        OrderEntity entity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        return orderMapper.toDomain(entity);
    }

    /**
     * 後台:標記訂單為已出貨
     *
     * @param orderNumber 訂單編號
     * @throws OrderNotFoundException 訂單不存在
     */
    @Transactional
    public void shipOrder(String orderNumber) {
        OrderEntity entity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        Order order = orderMapper.toDomain(entity);

        // 領域模型處理出貨邏輯(會驗證狀態)
        order.ship();

        // 更新實體
        entity.setStatus(order.getStatus());
        entity.setUpdatedAt(order.getUpdatedAt());

        orderRepository.save(entity);
    }

    /**
     * 後台:取消訂單(含原因)
     *
     * @param orderNumber 訂單編號
     * @param reason      取消原因
     * @throws OrderNotFoundException 訂單不存在
     */
    @Transactional
    public void cancelOrderAdmin(String orderNumber, String reason) {
        OrderEntity entity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        Order order = orderMapper.toDomain(entity);

        // 領域模型處理取消邏輯(會驗證狀態)
        order.cancel(reason);

        // 更新實體
        entity.setStatus(order.getStatus());
        entity.setCancellationReason(order.getCancellationReason());
        entity.setUpdatedAt(order.getUpdatedAt());

        orderRepository.save(entity);
    }
}
