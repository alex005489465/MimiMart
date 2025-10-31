package com.mimimart.infrastructure.persistence.mapper;

import com.mimimart.domain.cart.model.Cart;
import com.mimimart.domain.cart.model.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 購物車映射器 (基礎設施層)
 * 負責領域模型與 JPA 實體之間的轉換
 *
 * 設計理念:
 * - 隔離領域模型與持久化技術
 * - 領域模型不依賴 JPA 註解
 * - 基礎設施層負責資料轉換
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
@Component
public class CartMapper {

    /**
     * 將 JPA 實體列表轉換為領域模型
     *
     * @param cartItemEntities 購物車項目 JPA 實體列表
     * @param memberId 會員 ID
     * @return 購物車領域模型
     */
    public Cart toDomain(List<com.mimimart.infrastructure.persistence.entity.CartItem> cartItemEntities, Long memberId) {
        if (cartItemEntities == null || cartItemEntities.isEmpty()) {
            return Cart.create(memberId);
        }

        // 轉換 JPA 實體為領域值對象
        List<CartItem> cartItems = cartItemEntities.stream()
                .map(this::toCartItemDomain)
                .collect(Collectors.toList());

        // 重建購物車領域模型
        return Cart.reconstruct(memberId, cartItems);
    }

    /**
     * 將購物車項目 JPA 實體轉換為值對象
     */
    private CartItem toCartItemDomain(com.mimimart.infrastructure.persistence.entity.CartItem entity) {
        return CartItem.of(entity.getProductId(), entity.getQuantity());
    }

    /**
     * 將領域模型轉換為 JPA 實體列表
     *
     * @param cart 購物車領域模型
     * @return 購物車項目 JPA 實體列表
     */
    public List<com.mimimart.infrastructure.persistence.entity.CartItem> toEntities(Cart cart) {
        if (cart == null || cart.isEmpty()) {
            return List.of();
        }

        Long memberId = cart.getMemberId();

        return cart.getItems().stream()
                .map(item -> toCartItemEntity(item, memberId))
                .collect(Collectors.toList());
    }

    /**
     * 將購物車項目值對象轉換為 JPA 實體
     */
    private com.mimimart.infrastructure.persistence.entity.CartItem toCartItemEntity(CartItem cartItem, Long memberId) {
        return new com.mimimart.infrastructure.persistence.entity.CartItem(
                memberId,
                cartItem.getProductId(),
                cartItem.getQuantity()
        );
    }

    /**
     * 更新 JPA 實體 (用於修改數量)
     *
     * @param entity 現有 JPA 實體
     * @param cartItem 領域值對象
     * @return 更新後的 JPA 實體
     */
    public com.mimimart.infrastructure.persistence.entity.CartItem updateEntity(
            com.mimimart.infrastructure.persistence.entity.CartItem entity,
            CartItem cartItem) {

        entity.setQuantity(cartItem.getQuantity());
        return entity;
    }
}
