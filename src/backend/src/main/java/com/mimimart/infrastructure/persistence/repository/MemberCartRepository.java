package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.MemberCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 會員購物車資料存取介面
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface MemberCartRepository extends JpaRepository<MemberCart, Long> {

    /**
     * 根據會員 ID 查詢購物車
     * @param memberId 會員 ID
     * @return 購物車（可能不存在）
     */
    Optional<MemberCart> findByMemberId(Long memberId);

    /**
     * 檢查會員購物車是否存在
     * @param memberId 會員 ID
     * @return 是否存在
     */
    boolean existsByMemberId(Long memberId);

    /**
     * 刪除指定會員的購物車
     * @param memberId 會員 ID
     */
    void deleteByMemberId(Long memberId);
}
