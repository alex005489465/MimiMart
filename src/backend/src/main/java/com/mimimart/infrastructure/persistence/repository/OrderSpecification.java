package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.domain.order.model.OrderStatus;
import com.mimimart.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 訂單動態查詢規格
 * 用於後台訂單列表的多條件篩選
 */
public class OrderSpecification {

    /**
     * 根據多個條件動態建立查詢規格
     *
     * @param status      訂單狀態(可選)
     * @param orderNumber 訂單編號(模糊搜尋,可選)
     * @param startDate   開始日期(可選)
     * @param endDate     結束日期(可選)
     * @return Specification
     */
    public static Specification<OrderEntity> withFilters(
            OrderStatus status,
            String orderNumber,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 狀態篩選
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 訂單編號模糊搜尋（跳脫 LIKE 特殊字元）
            if (orderNumber != null && !orderNumber.isBlank()) {
                String escapedOrderNumber = orderNumber
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                predicates.add(criteriaBuilder.like(root.get("orderNumber"), "%" + escapedOrderNumber + "%"));
            }

            // 日期範圍篩選
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
