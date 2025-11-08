package com.mimimart.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 購物車項目 Repository
 * 使用 Redis Hash 儲存購物車項目
 * Key: cart:{memberId}
 * Field: {productId} → Value: {quantity}|{addedAt_timestamp}
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisCartItemRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_DAYS = 7;
    private static final String FIELD_SEPARATOR = "|";

    /**
     * 取得購物車 Redis Key
     */
    private String getCartKey(Long memberId) {
        return CART_KEY_PREFIX + memberId;
    }

    /**
     * 編碼值: quantity|timestamp
     */
    private String encodeValue(Integer quantity, Long addedAt) {
        return quantity + FIELD_SEPARATOR + addedAt;
    }

    /**
     * 解碼值: {quantity, timestamp}
     */
    private CartItemValue decodeValue(String value) {
        String[] parts = value.split("\\" + FIELD_SEPARATOR);
        if (parts.length != 2) {
            log.warn("Invalid cart item value format: {}", value);
            // 容錯處理:若格式錯誤,回傳預設值
            return new CartItemValue(1, Instant.now().toEpochMilli());
        }
        return new CartItemValue(
            Integer.parseInt(parts[0]),
            Long.parseLong(parts[1])
        );
    }

    /**
     * 新增或更新購物車項目
     */
    public void save(Long memberId, Long productId, Integer quantity) {
        String key = getCartKey(memberId);
        Long addedAt = Instant.now().toEpochMilli();
        String value = encodeValue(quantity, addedAt);

        redisTemplate.opsForHash().put(key, String.valueOf(productId), value);
        // 更新 TTL
        redisTemplate.expire(key, CART_TTL_DAYS, TimeUnit.DAYS);

        log.debug("Saved cart item: memberId={}, productId={}, quantity={}", memberId, productId, quantity);
    }

    /**
     * 取得購物車項目數量
     */
    public Integer getQuantity(Long memberId, Long productId) {
        String key = getCartKey(memberId);
        Object value = redisTemplate.opsForHash().get(key, String.valueOf(productId));

        if (value == null) {
            return null;
        }

        CartItemValue cartItemValue = decodeValue(value.toString());
        return cartItemValue.quantity;
    }

    /**
     * 取得購物車所有項目
     * @return Map<productId, CartItemValue>
     */
    public Map<Long, CartItemValue> findAllByMemberId(Long memberId) {
        String key = getCartKey(memberId);
        Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);

        Map<Long, CartItemValue> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            Long productId = Long.parseLong(entry.getKey().toString());
            CartItemValue value = decodeValue(entry.getValue().toString());
            result.put(productId, value);
        }

        log.debug("Found {} cart items for memberId={}", result.size(), memberId);
        return result;
    }

    /**
     * 刪除購物車項目
     */
    public void delete(Long memberId, Long productId) {
        String key = getCartKey(memberId);
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));

        log.debug("Deleted cart item: memberId={}, productId={}", memberId, productId);
    }

    /**
     * 清空購物車
     */
    public void deleteAll(Long memberId) {
        String key = getCartKey(memberId);
        redisTemplate.delete(key);

        log.debug("Cleared cart for memberId={}", memberId);
    }

    /**
     * 取得購物車項目數量
     */
    public Long countByMemberId(Long memberId) {
        String key = getCartKey(memberId);
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 檢查購物車項目是否存在
     */
    public boolean exists(Long memberId, Long productId) {
        String key = getCartKey(memberId);
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, String.valueOf(productId)));
    }

    /**
     * 購物車項目值物件
     */
    public static class CartItemValue {
        public final Integer quantity;
        public final Long addedAt;

        public CartItemValue(Integer quantity, Long addedAt) {
            this.quantity = quantity;
            this.addedAt = addedAt;
        }
    }
}
