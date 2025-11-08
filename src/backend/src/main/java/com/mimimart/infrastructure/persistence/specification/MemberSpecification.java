package com.mimimart.infrastructure.persistence.specification;

import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.shared.valueobject.MemberStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 會員查詢條件建構器
 * 使用 JPA Specification 實現動態查詢
 */
public class MemberSpecification {

    /**
     * 建立會員查詢條件
     *
     * @param keyword    搜尋關鍵字（支援 Email、姓名、電話、會員 ID）
     * @param status     帳號狀態篩選
     * @param startDate  註冊開始日期
     * @param endDate    註冊結束日期
     * @return Specification
     */
    public static Specification<Member> buildSpecification(
            String keyword,
            MemberStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 關鍵字搜尋（Email、姓名、電話、會員 ID）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim() + "%";
                Predicate keywordPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(root.get("email"), likePattern),
                        criteriaBuilder.like(root.get("name"), likePattern),
                        criteriaBuilder.like(root.get("phone"), likePattern)
                );

                // 如果關鍵字是數字，也搜尋會員 ID
                try {
                    Long memberId = Long.parseLong(keyword.trim());
                    keywordPredicate = criteriaBuilder.or(
                            keywordPredicate,
                            criteriaBuilder.equal(root.get("id"), memberId)
                    );
                } catch (NumberFormatException e) {
                    // 不是數字，忽略 ID 搜尋
                }

                predicates.add(keywordPredicate);
            }

            // 帳號狀態篩選
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 註冊時間範圍篩選
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
