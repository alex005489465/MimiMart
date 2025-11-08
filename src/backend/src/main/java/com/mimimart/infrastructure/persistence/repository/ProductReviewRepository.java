package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品評價資料存取介面
 */
@Repository
public interface ProductReviewRepository extends
        JpaRepository<ProductReview, Long>,
        JpaSpecificationExecutor<ProductReview> {

    /**
     * 唯一性檢查：檢查訂單項目是否已評價
     * 用於應用層唯一性約束
     */
    boolean existsByOrderItemId(Long orderItemId);

    /**
     * 根據 ID 查詢可見的評價
     */
    Optional<ProductReview> findByIdAndIsVisibleTrue(Long id);

    /**
     * 查詢商品的所有可見評價（分頁）
     */
    Page<ProductReview> findByProductIdAndIsVisibleTrue(Long productId, Pageable pageable);

    /**
     * 查詢會員的所有評價（分頁）
     */
    Page<ProductReview> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 查詢會員對特定商品的評價
     */
    List<ProductReview> findByMemberIdAndProductId(Long memberId, Long productId);

    /**
     * 查詢商品評價統計
     * 回傳: [評價數量, 平均評分]
     */
    @Query("SELECT COUNT(r), COALESCE(AVG(r.rating), 0.0) FROM ProductReview r " +
           "WHERE r.productId = :productId AND r.isVisible = true")
    Object[] getReviewStats(@Param("productId") Long productId);

    /**
     * 查詢商品評分分布
     * 回傳: List<[rating, count]>
     */
    @Query("SELECT r.rating, COUNT(r) FROM ProductReview r " +
           "WHERE r.productId = :productId AND r.isVisible = true " +
           "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    /**
     * 計算商品的總評價數（含隱藏）
     */
    long countByProductId(Long productId);

    /**
     * 計算商品的可見評價數
     */
    long countByProductIdAndIsVisibleTrue(Long productId);
}
