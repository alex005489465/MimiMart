package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 訂單 Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    /**
     * 根據訂單編號查詢訂單
     */
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    /**
     * 查詢會員的所有訂單(按建立時間降序)
     */
    List<OrderEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 檢查訂單編號是否已存在
     */
    boolean existsByOrderNumber(String orderNumber);
}
