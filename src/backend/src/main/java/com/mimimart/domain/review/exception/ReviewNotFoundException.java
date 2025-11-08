package com.mimimart.domain.review.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 評價不存在異常
 */
public class ReviewNotFoundException extends DomainException {

    public ReviewNotFoundException(String message) {
        super("REVIEW_NOT_FOUND", message);
    }

    public ReviewNotFoundException(Long reviewId) {
        super("REVIEW_NOT_FOUND", "評價不存在: ID=" + reviewId);
    }
}
