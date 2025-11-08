package com.mimimart.domain.review.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效評分異常
 * 用於驗證評分必須在 1-5 範圍內
 */
public class InvalidRatingException extends DomainException {

    public InvalidRatingException(String message) {
        super("INVALID_RATING", message);
    }

    public InvalidRatingException(int rating) {
        super("INVALID_RATING", "評分必須介於 1-5 之間，實際值: " + rating);
    }
}
