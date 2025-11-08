package com.mimimart.domain.cart.model;

import com.mimimart.domain.cart.exception.InvalidQuantityException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 購物車項目值對象 (不可變)
 * 封裝單一購物車項目的業務邏輯
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
public class CartItem {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY_PER_ITEM = 999;

    private final Long productId;
    private final Integer quantity;
    private final LocalDateTime addedAt;

    /**
     * 私有建構函式,確保透過工廠方法建立
     */
    private CartItem(Long productId, Integer quantity, LocalDateTime addedAt) {
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    /**
     * 工廠方法:建立購物車項目 (使用當前時間)
     *
     * @param productId 商品 ID
     * @param quantity 數量
     * @return CartItem
     * @throws InvalidQuantityException 數量不合法時拋出
     */
    public static CartItem of(Long productId, Integer quantity) {
        return of(productId, quantity, LocalDateTime.now());
    }

    /**
     * 工廠方法:建立購物車項目 (指定加入時間)
     *
     * @param productId 商品 ID
     * @param quantity 數量
     * @param addedAt 加入時間
     * @return CartItem
     * @throws InvalidQuantityException 數量不合法時拋出
     */
    public static CartItem of(Long productId, Integer quantity, LocalDateTime addedAt) {
        Objects.requireNonNull(productId, "商品 ID 不能為 null");
        Objects.requireNonNull(quantity, "數量不能為 null");
        Objects.requireNonNull(addedAt, "加入時間不能為 null");

        validateQuantity(quantity);

        return new CartItem(productId, quantity, addedAt);
    }

    /**
     * 工廠方法:從 timestamp 建立購物車項目
     *
     * @param productId 商品 ID
     * @param quantity 數量
     * @param addedAtTimestamp 加入時間 (毫秒級 timestamp)
     * @return CartItem
     */
    public static CartItem fromTimestamp(Long productId, Integer quantity, Long addedAtTimestamp) {
        LocalDateTime addedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(addedAtTimestamp),
            ZoneId.systemDefault()
        );
        return of(productId, quantity, addedAt);
    }

    /**
     * 更新數量 (回傳新的不可變對象,保留原加入時間)
     *
     * @param newQuantity 新數量
     * @return 新的 CartItem
     * @throws InvalidQuantityException 數量不合法時拋出
     */
    public CartItem withQuantity(Integer newQuantity) {
        validateQuantity(newQuantity);
        return new CartItem(this.productId, newQuantity, this.addedAt);
    }

    /**
     * 增加數量 (回傳新的不可變對象,保留原加入時間)
     *
     * @param additionalQuantity 要增加的數量
     * @return 新的 CartItem
     * @throws InvalidQuantityException 累加後數量超過上限時拋出
     */
    public CartItem addQuantity(Integer additionalQuantity) {
        Objects.requireNonNull(additionalQuantity, "增加的數量不能為 null");

        int newQuantity = this.quantity + additionalQuantity;

        if (newQuantity > MAX_QUANTITY_PER_ITEM) {
            throw InvalidQuantityException.exceedsLimit(newQuantity, MAX_QUANTITY_PER_ITEM);
        }

        validateQuantity(newQuantity);
        return new CartItem(this.productId, newQuantity, this.addedAt);
    }

    /**
     * 驗證數量是否合法
     */
    private static void validateQuantity(Integer quantity) {
        if (quantity < MIN_QUANTITY) {
            throw InvalidQuantityException.tooSmall(MIN_QUANTITY);
        }
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw InvalidQuantityException.tooLarge(MAX_QUANTITY_PER_ITEM);
        }
    }

    // Getters

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public static int getMaxQuantityPerItem() {
        return MAX_QUANTITY_PER_ITEM;
    }

    // equals & hashCode (基於 productId)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(productId, cartItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", addedAt=" + addedAt +
                '}';
    }
}
