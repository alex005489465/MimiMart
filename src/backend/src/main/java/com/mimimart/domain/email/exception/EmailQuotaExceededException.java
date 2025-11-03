package com.mimimart.domain.email.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 郵件配額超限異常
 */
public class EmailQuotaExceededException extends DomainException {
    public EmailQuotaExceededException(String message) {
        super("EMAIL_QUOTA_EXCEEDED", message);
    }
}
