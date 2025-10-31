package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 購物車項目資料存取介面
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 查詢指定會員的所有購物車項目
     * @param memberId 會員 ID
     * @return 購物車項目列表
     */
    List<CartItem> findByMemberId(Long memberId);

    /**
     * 查詢指定會員的特定商品購物車項目
     * @param memberId 會員 ID
     * @param productId 商品 ID
     * @return 購物車項目（可能不存在）
     */
    Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

    /**
     * 刪除指定會員的所有購物車項目
     * @param memberId 會員 ID
     */
    void deleteByMemberId(Long memberId);

    /**
     * 檢查指定會員的購物車是否包含特定商品
     * @param memberId 會員 ID
     * @param productId 商品 ID
     * @return 是否存在
     */
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}
