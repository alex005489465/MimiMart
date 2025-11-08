package com.mimimart.domain.review.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 未經授權的評價操作異常
 * 用於驗證評價的擁有權（不能修改/刪除他人的評價）
 */
public class UnauthorizedReviewException extends DomainException {

    public UnauthorizedReviewException(String message) {
        super("UNAUTHORIZED_REVIEW", message);
    }
}
