package com.mimimart.domain.review.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 評價已存在異常
 * 用於防止同一訂單項目重複評價
 */
public class ReviewAlreadyExistsException extends DomainException {

    public ReviewAlreadyExistsException(String message) {
        super("REVIEW_ALREADY_EXISTS", message);
    }

    public ReviewAlreadyExistsException(Long orderItemId) {
        super("REVIEW_ALREADY_EXISTS", "該商品已評價過: orderItemId=" + orderItemId);
    }
}
