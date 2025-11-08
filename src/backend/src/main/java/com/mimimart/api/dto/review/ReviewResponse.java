package com.mimimart.api.dto.review;

import com.mimimart.domain.review.model.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 評價回應 DTO
 * 用於評價列表顯示
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long memberId;
    private Integer rating;
    private String content;
    private String adminReply;
    private LocalDateTime adminRepliedAt;
    private boolean isVisible;
    private LocalDateTime createdAt;

    /**
     * 從領域模型建立 DTO
     */
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getProductId(),
            review.getMemberId(),
            review.getRating().getValue(),
            review.getContent(),
            review.getAdminReply(),
            review.getAdminRepliedAt(),
            review.isVisible(),
            review.getCreatedAt()
        );
    }
}
