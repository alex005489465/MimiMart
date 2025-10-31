package com.mimimart.domain.cart.service;

import com.mimimart.domain.cart.model.Cart;
import com.mimimart.domain.cart.model.CartItem;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 購物車合併領域服務
 * 處理訪客購物車與會員購物車的合併邏輯
 *
 * 設計理念:
 * - 領域服務處理跨聚合的業務邏輯
 * - 合併策略:相同商品取較大數量,未超過上限則累加
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
@Service
public class CartMergeService {

    /**
     * 合併訪客購物車到會員購物車
     *
     * 合併策略:
     * 1. 遍歷訪客購物車的所有項目
     * 2. 若會員購物車已有該商品,取較大數量 (若累加不超過上限則累加,否則取上限值)
     * 3. 若會員購物車沒有該商品,直接加入
     *
     * @param memberCart 會員購物車 (會被修改)
     * @param guestCart 訪客購物車
     * @return 合併後的會員購物車
     */
    public Cart merge(Cart memberCart, Cart guestCart) {
        Objects.requireNonNull(memberCart, "會員購物車不能為 null");
        Objects.requireNonNull(guestCart, "訪客購物車不能為 null");

        // 遍歷訪客購物車的所有項目
        for (CartItem guestItem : guestCart.getItems()) {
            Long productId = guestItem.getProductId();
            Integer guestQuantity = guestItem.getQuantity();

            if (memberCart.containsProduct(productId)) {
                // 已存在:取較大數量,或嘗試累加
                int memberQuantity = memberCart.getQuantity(productId);
                int maxQuantity = CartItem.getMaxQuantityPerItem();

                // 嘗試累加,若不超過上限
                int newQuantity = Math.min(memberQuantity + guestQuantity, maxQuantity);

                // 如果累加後會超過上限,則取較大數量即可
                if (memberQuantity + guestQuantity > maxQuantity) {
                    newQuantity = Math.max(memberQuantity, guestQuantity);
                    // 若仍然超過上限,則使用上限值
                    newQuantity = Math.min(newQuantity, maxQuantity);
                }

                memberCart.updateQuantity(productId, newQuantity);
            } else {
                // 不存在:直接加入
                memberCart.addProduct(productId, guestQuantity);
            }
        }

        return memberCart;
    }

    /**
     * 合併訪客購物車到會員購物車 (累加策略)
     * 相同商品直接累加數量,若超過上限則使用上限值
     *
     * @param memberCart 會員購物車 (會被修改)
     * @param guestCart 訪客購物車
     * @return 合併後的會員購物車
     */
    public Cart mergeByAddition(Cart memberCart, Cart guestCart) {
        Objects.requireNonNull(memberCart, "會員購物車不能為 null");
        Objects.requireNonNull(guestCart, "訪客購物車不能為 null");

        for (CartItem guestItem : guestCart.getItems()) {
            Long productId = guestItem.getProductId();
            Integer guestQuantity = guestItem.getQuantity();

            try {
                // 嘗試直接加入 (若已存在會自動累加)
                memberCart.addProduct(productId, guestQuantity);
            } catch (com.mimimart.domain.cart.exception.InvalidQuantityException e) {
                // 若累加超過上限,則設定為上限值
                int maxQuantity = CartItem.getMaxQuantityPerItem();

                if (memberCart.containsProduct(productId)) {
                    memberCart.updateQuantity(productId, maxQuantity);
                } else {
                    memberCart.addProduct(productId, Math.min(guestQuantity, maxQuantity));
                }
            }
        }

        return memberCart;
    }

    /**
     * 合併訪客購物車到會員購物車 (覆蓋策略)
     * 相同商品以訪客購物車的數量為準
     *
     * @param memberCart 會員購物車 (會被修改)
     * @param guestCart 訪客購物車
     * @return 合併後的會員購物車
     */
    public Cart mergeByOverwrite(Cart memberCart, Cart guestCart) {
        Objects.requireNonNull(memberCart, "會員購物車不能為 null");
        Objects.requireNonNull(guestCart, "訪客購物車不能為 null");

        for (CartItem guestItem : guestCart.getItems()) {
            Long productId = guestItem.getProductId();
            Integer guestQuantity = guestItem.getQuantity();

            if (memberCart.containsProduct(productId)) {
                memberCart.updateQuantity(productId, guestQuantity);
            } else {
                memberCart.addProduct(productId, guestQuantity);
            }
        }

        return memberCart;
    }
}
