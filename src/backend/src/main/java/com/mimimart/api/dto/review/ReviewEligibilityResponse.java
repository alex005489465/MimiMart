package com.mimimart.api.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 評價資格檢查回應 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEligibilityResponse {

    /**
     * 是否可以評價
     */
    private boolean canReview;

    /**
     * 不能評價的原因（canReview=false 時提供）
     */
    private String reason;
}
