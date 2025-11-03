package com.mimimart.domain.email.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 郵件發送頻率超限異常
 */
public class EmailRateLimitExceededException extends DomainException {
    public EmailRateLimitExceededException(String message) {
        super("EMAIL_RATE_LIMIT_EXCEEDED", message);
    }
}
