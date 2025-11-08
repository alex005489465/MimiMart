package com.mimimart.domain.review.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 評價資格不符異常
 * 用於驗證評價權限（未購買、超過期限、未送達等）
 */
public class ReviewNotEligibleException extends DomainException {

    public ReviewNotEligibleException(String message) {
        super("REVIEW_NOT_ELIGIBLE", message);
    }
}
