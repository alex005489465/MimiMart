package com.mimimart.domain.product.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 分類不存在異常
 */
public class CategoryNotFoundException extends DomainException {

    public CategoryNotFoundException(String message) {
        super("CATEGORY_NOT_FOUND", message);
    }

    public CategoryNotFoundException(Long categoryId) {
        super("CATEGORY_NOT_FOUND", "分類不存在: ID=" + categoryId);
    }
}
