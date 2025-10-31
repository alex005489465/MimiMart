package com.mimimart.domain.cart.model;

import com.mimimart.domain.cart.exception.CartItemNotFoundException;
import com.mimimart.domain.cart.exception.InvalidQuantityException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 購物車領域模型 (聚合根)
 * 封裝購物車相關業務邏輯
 *
 * 設計理念:
 * - 使用有業務語義的方法,而非 setter
 * - 封裝業務規則驗證邏輯
 * - 確保領域模型狀態永遠有效
 * - 使用不可變的 CartItem 值對象
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
public class Cart {

    private final Long memberId;
    private final Map<Long, CartItem> items; // Key: productId, Value: CartItem

    /**
     * 私有建構函式,強制透過工廠方法建立
     */
    private Cart(Long memberId) {
        this.memberId = memberId;
        this.items = new HashMap<>();
    }

    // ========================================
    // 工廠方法 (Factory Methods)
    // ========================================

    /**
     * 工廠方法:建立新購物車
     *
     * @param memberId 會員 ID
     * @return 新購物車
     */
    public static Cart create(Long memberId) {
        Objects.requireNonNull(memberId, "會員 ID 不能為 null");
        return new Cart(memberId);
    }

    /**
     * 工廠方法:從持久化資料重建購物車
     *
     * @param memberId 會員 ID
     * @param items 購物車項目列表
     * @return 購物車
     */
    public static Cart reconstruct(Long memberId, List<CartItem> items) {
        Objects.requireNonNull(memberId, "會員 ID 不能為 null");
        Objects.requireNonNull(items, "購物車項目列表不能為 null");

        Cart cart = new Cart(memberId);
        for (CartItem item : items) {
            cart.items.put(item.getProductId(), item);
        }
        return cart;
    }

    // ========================================
    // 業務方法 (Business Methods)
    // ========================================

    /**
     * 加入商品至購物車
     * - 若商品已存在,則累加數量
     * - 若商品不存在,則新增項目
     *
     * @param productId 商品 ID
     * @param quantity 數量
     * @throws InvalidQuantityException 數量不合法或累加後超過上限時拋出
     */
    public void addProduct(Long productId, Integer quantity) {
        Objects.requireNonNull(productId, "商品 ID 不能為 null");
        Objects.requireNonNull(quantity, "數量不能為 null");

        if (items.containsKey(productId)) {
            // 已存在:累加數量
            CartItem existingItem = items.get(productId);
            CartItem updatedItem = existingItem.addQuantity(quantity);
            items.put(productId, updatedItem);
        } else {
            // 不存在:新增項目
            CartItem newItem = CartItem.of(productId, quantity);
            items.put(productId, newItem);
        }
    }

    /**
     * 更新購物車項目數量
     *
     * @param productId 商品 ID
     * @param quantity 新數量
     * @throws CartItemNotFoundException 商品不存在時拋出
     * @throws InvalidQuantityException 數量不合法時拋出
     */
    public void updateQuantity(Long productId, Integer quantity) {
        Objects.requireNonNull(productId, "商品 ID 不能為 null");
        Objects.requireNonNull(quantity, "數量不能為 null");

        if (!items.containsKey(productId)) {
            throw new CartItemNotFoundException(productId);
        }

        CartItem updatedItem = CartItem.of(productId, quantity);
        items.put(productId, updatedItem);
    }

    /**
     * 移除購物車項目
     *
     * @param productId 商品 ID
     * @throws CartItemNotFoundException 商品不存在時拋出
     */
    public void removeProduct(Long productId) {
        Objects.requireNonNull(productId, "商品 ID 不能為 null");

        if (!items.containsKey(productId)) {
            throw new CartItemNotFoundException(productId);
        }

        items.remove(productId);
    }

    /**
     * 清空購物車
     */
    public void clear() {
        items.clear();
    }

    /**
     * 取得購物車項目總數量 (所有商品數量加總)
     *
     * @return 總數量
     */
    public int getTotalItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * 檢查購物車是否為空
     *
     * @return true: 空, false: 非空
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 取得購物車項目數量 (不同商品種類數量)
     *
     * @return 項目數量
     */
    public int getItemSize() {
        return items.size();
    }

    /**
     * 檢查是否包含指定商品
     *
     * @param productId 商品 ID
     * @return true: 包含, false: 不包含
     */
    public boolean containsProduct(Long productId) {
        return items.containsKey(productId);
    }

    /**
     * 取得指定商品的數量
     *
     * @param productId 商品 ID
     * @return 數量,若商品不存在則回傳 0
     */
    public int getQuantity(Long productId) {
        if (!items.containsKey(productId)) {
            return 0;
        }
        return items.get(productId).getQuantity();
    }

    // ========================================
    // Getters
    // ========================================

    public Long getMemberId() {
        return memberId;
    }

    /**
     * 取得購物車項目列表 (不可變視圖)
     *
     * @return 購物車項目列表
     */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    @Override
    public String toString() {
        return "Cart{" +
                "memberId=" + memberId +
                ", itemCount=" + items.size() +
                ", totalQuantity=" + getTotalItemCount() +
                '}';
    }
}
