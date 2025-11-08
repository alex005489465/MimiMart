package com.mimimart.api.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 評價統計回應 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {

    /**
     * 總評價數
     */
    private Long totalReviews;

    /**
     * 平均評分
     */
    private Double avgRating;

    /**
     * 評分分布
     * key: 評分 (1-5)
     * value: 該評分的數量
     */
    private Map<Integer, Long> ratingDistribution;

    /**
     * 建立統計回應
     */
    public static ReviewStatsResponse create(Long totalReviews, Double avgRating, Map<Integer, Long> distribution) {
        return new ReviewStatsResponse(totalReviews, avgRating, distribution);
    }
}
