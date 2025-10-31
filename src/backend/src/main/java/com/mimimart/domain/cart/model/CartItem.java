package com.mimimart.domain.cart.model;

import com.mimimart.domain.cart.exception.InvalidQuantityException;

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

    /**
     * 私有建構函式,確保透過工廠方法建立
     */
    private CartItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    /**
     * 工廠方法:建立購物車項目
     *
     * @param productId 商品 ID
     * @param quantity 數量
     * @return CartItem
     * @throws InvalidQuantityException 數量不合法時拋出
     */
    public static CartItem of(Long productId, Integer quantity) {
        Objects.requireNonNull(productId, "商品 ID 不能為 null");
        Objects.requireNonNull(quantity, "數量不能為 null");

        validateQuantity(quantity);

        return new CartItem(productId, quantity);
    }

    /**
     * 更新數量 (回傳新的不可變對象)
     *
     * @param newQuantity 新數量
     * @return 新的 CartItem
     * @throws InvalidQuantityException 數量不合法時拋出
     */
    public CartItem withQuantity(Integer newQuantity) {
        validateQuantity(newQuantity);
        return new CartItem(this.productId, newQuantity);
    }

    /**
     * 增加數量 (回傳新的不可變對象)
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
        return new CartItem(this.productId, newQuantity);
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
                '}';
    }
}
