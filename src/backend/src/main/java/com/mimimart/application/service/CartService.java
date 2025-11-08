package com.mimimart.application.service;

import com.mimimart.api.dto.cart.*;
import com.mimimart.domain.cart.exception.CartFullException;
import com.mimimart.domain.cart.exception.CartItemNotFoundException;
import com.mimimart.domain.cart.model.CartItem;
import com.mimimart.domain.product.exception.ProductNotFoundException;
import com.mimimart.infrastructure.persistence.entity.MemberCart;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.persistence.repository.MemberCartRepository;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import com.mimimart.infrastructure.persistence.repository.RedisCartItemRepository;
import com.mimimart.infrastructure.persistence.repository.RedisCartItemRepository.CartItemValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 購物車應用服務 (使用 Redis 儲存購物車項目)
 * 職責: 協調 Redis、資料庫、領域模型
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisCartItemRepository redisCartItemRepository;
    private final MemberCartRepository memberCartRepository;
    private final ProductRepository productRepository;

    /**
     * 查詢會員的購物車摘要(含商品完整資訊)
     * 流程: Redis → 批次查詢商品 → 組合 DTO
     */
    public CartSummaryDTO getCart(Long memberId) {
        // 1. 從 Redis 載入購物車項目
        Map<Long, CartItemValue> cartItems = redisCartItemRepository.findAllByMemberId(memberId);

        if (cartItems.isEmpty()) {
            return CartSummaryDTO.builder()
                    .items(Collections.emptyList())
                    .totalItems(0)
                    .totalQuantity(0)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        // 2. 批次查詢商品資訊 (優化 N+1 查詢)
        Set<Long> productIds = cartItems.keySet();
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3. 組合 CartItemDTO (同步最新價格)
        List<CartItemDTO> itemDTOs = cartItems.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    CartItemValue value = entry.getValue();
                    Product product = productMap.get(productId);

                    // 若商品已被刪除,跳過該項目
                    if (product == null) {
                        log.warn("Product {} in cart not found, skipping", productId);
                        return null;
                    }

                    return buildCartItemDTO(product, value);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CartItemDTO::getAddedAt).reversed()) // 按加入時間倒序
                .collect(Collectors.toList());

        // 4. 回傳摘要
        return CartSummaryDTO.from(itemDTOs);
    }

    /**
     * 加入商品至購物車
     * - 若商品已存在,則累加數量
     * - 若商品不存在,則新增項目
     */
    @Transactional
    public CartItemDTO addToCart(Long memberId, AddToCartRequest request) {
        // 1. 驗證商品是否存在
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        // 2. 確保會員購物車主表存在
        MemberCart memberCart = getOrCreateMemberCart(memberId);

        // 3. 檢查購物車項目數量上限
        Long currentItemsCount = redisCartItemRepository.countByMemberId(memberId);
        boolean isNewItem = !redisCartItemRepository.exists(memberId, request.getProductId());

        if (isNewItem && currentItemsCount >= memberCart.getMaxItemsCount()) {
            throw new CartFullException(memberCart.getMaxItemsCount());
        }

        // 4. 建立領域模型並驗證數量
        Integer currentQuantity = redisCartItemRepository.getQuantity(memberId, request.getProductId());
        Integer newQuantity;

        if (currentQuantity == null) {
            // 新增項目
            CartItem.of(request.getProductId(), request.getQuantity()); // 驗證數量
            newQuantity = request.getQuantity();
        } else {
            // 累加數量
            CartItem existingItem = CartItem.of(request.getProductId(), currentQuantity);
            CartItem updatedItem = existingItem.addQuantity(request.getQuantity());
            newQuantity = updatedItem.getQuantity();
        }

        // 5. 儲存至 Redis
        redisCartItemRepository.save(memberId, request.getProductId(), newQuantity);

        // 6. 更新主表時間戳
        memberCart.setUpdatedAt(java.time.LocalDateTime.now());
        memberCartRepository.save(memberCart);

        // 7. 回傳 DTO (包含庫存警告)
        CartItemValue cartItemValue = new CartItemValue(newQuantity, System.currentTimeMillis());
        return buildCartItemDTO(product, cartItemValue);
    }

    /**
     * 更新購物車項目數量
     */
    @Transactional
    public CartItemDTO updateQuantity(Long memberId, UpdateCartItemRequest request) {
        Long productId = request.getProductId();
        Integer newQuantity = request.getQuantity();

        // 1. 驗證商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 2. 檢查購物車項目是否存在
        if (!redisCartItemRepository.exists(memberId, productId)) {
            throw new CartItemNotFoundException(productId);
        }

        // 3. 建立領域模型並驗證數量
        CartItem.of(productId, newQuantity); // 驗證數量

        // 4. 更新 Redis
        redisCartItemRepository.save(memberId, productId, newQuantity);

        // 5. 更新主表時間戳
        MemberCart memberCart = getOrCreateMemberCart(memberId);
        memberCart.setUpdatedAt(java.time.LocalDateTime.now());
        memberCartRepository.save(memberCart);

        // 6. 回傳 DTO
        CartItemValue cartItemValue = new CartItemValue(newQuantity, System.currentTimeMillis());
        return buildCartItemDTO(product, cartItemValue);
    }

    /**
     * 移除購物車項目
     */
    @Transactional
    public void removeItem(Long memberId, RemoveCartItemRequest request) {
        Long productId = request.getProductId();

        // 1. 檢查購物車項目是否存在
        if (!redisCartItemRepository.exists(memberId, productId)) {
            throw new CartItemNotFoundException(productId);
        }

        // 2. 從 Redis 刪除
        redisCartItemRepository.delete(memberId, productId);

        // 3. 更新主表時間戳
        MemberCart memberCart = getOrCreateMemberCart(memberId);
        memberCart.setUpdatedAt(java.time.LocalDateTime.now());
        memberCartRepository.save(memberCart);

        log.info("Removed product {} from cart for member {}", productId, memberId);
    }

    /**
     * 清空購物車
     */
    @Transactional
    public void clearCart(Long memberId) {
        // 從 Redis 清空
        redisCartItemRepository.deleteAll(memberId);

        // 更新主表時間戳
        MemberCart memberCart = getOrCreateMemberCart(memberId);
        memberCart.setUpdatedAt(java.time.LocalDateTime.now());
        memberCartRepository.save(memberCart);

        log.info("Cleared cart for member {}", memberId);
    }

    /**
     * 合併購物車(登入時將前端 LocalStorage 資料同步至後端)
     * 策略: 相同商品累加數量,不超過上限
     */
    @Transactional
    public CartSummaryDTO mergeCart(Long memberId, MergeCartRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return getCart(memberId);
        }

        // 1. 確保會員購物車主表存在
        MemberCart memberCart = getOrCreateMemberCart(memberId);

        // 2. 載入會員現有購物車
        Map<Long, CartItemValue> memberCartItems = redisCartItemRepository.findAllByMemberId(memberId);

        // 3. 驗證訪客購物車商品是否存在
        Set<Long> guestProductIds = request.getItems().stream()
                .map(MergeCartRequest.MergeCartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Long, Product> productMap = productRepository.findAllById(guestProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 4. 合併購物車項目
        for (MergeCartRequest.MergeCartItem guestItem : request.getItems()) {
            Long productId = guestItem.getProductId();
            Integer guestQuantity = guestItem.getQuantity();

            // 跳過不存在的商品
            if (!productMap.containsKey(productId)) {
                log.warn("Product {} not found during cart merge, skipping", productId);
                continue;
            }

            // 驗證數量
            try {
                CartItem.of(productId, guestQuantity);
            } catch (Exception e) {
                log.warn("Invalid quantity for product {} during merge, skipping", productId);
                continue;
            }

            // 合併邏輯: 累加數量
            CartItemValue memberValue = memberCartItems.get(productId);
            Integer finalQuantity;

            if (memberValue == null) {
                // 會員購物車沒有此商品,直接加入
                finalQuantity = guestQuantity;
            } else {
                // 累加數量,但不超過上限
                int maxQuantity = CartItem.getMaxQuantityPerItem();
                finalQuantity = Math.min(memberValue.quantity + guestQuantity, maxQuantity);
            }

            // 檢查購物車項目數量上限
            Long currentItemsCount = redisCartItemRepository.countByMemberId(memberId);
            if (memberValue == null && currentItemsCount >= memberCart.getMaxItemsCount()) {
                log.warn("Cart full, skipping product {} during merge", productId);
                continue;
            }

            // 儲存至 Redis
            redisCartItemRepository.save(memberId, productId, finalQuantity);
        }

        // 5. 更新主表時間戳
        memberCart.setUpdatedAt(java.time.LocalDateTime.now());
        memberCartRepository.save(memberCart);

        // 6. 回傳合併後的購物車
        return getCart(memberId);
    }

    /**
     * 取得或建立會員購物車主表
     */
    private MemberCart getOrCreateMemberCart(Long memberId) {
        return memberCartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    MemberCart newCart = new MemberCart(memberId);
                    return memberCartRepository.save(newCart);
                });
    }

    /**
     * 建立 CartItemDTO (包含商品資訊與庫存狀態)
     */
    private CartItemDTO buildCartItemDTO(Product product, CartItemValue cartItemValue) {
        Integer quantity = cartItemValue.quantity;
        BigDecimal price = product.getPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));

        // 計算庫存狀態
        Integer stock = product.getStock();
        Boolean isOutOfStock = stock < quantity;

        // 轉換加入時間
        java.time.LocalDateTime addedAt = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(cartItemValue.addedAt),
                java.time.ZoneId.systemDefault()
        );

        // 取得分類名稱 (若需要)
        String categoryName = null;
        if (product.getCategoryId() != null) {
            categoryName = productRepository.findById(product.getId())
                    .map(p -> p.getCategoryId().toString())
                    .orElse(null);
        }

        return CartItemDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(quantity)
                .price(price)
                .totalPrice(totalPrice)
                .imageUrl(product.getImageUrl())
                .categoryName(categoryName)
                .stock(stock)
                .isOutOfStock(isOutOfStock)
                .addedAt(addedAt)
                .build();
    }
}
