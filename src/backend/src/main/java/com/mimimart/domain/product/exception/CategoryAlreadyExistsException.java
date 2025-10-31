package com.mimimart.domain.product.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 分類已存在異常
 */
public class CategoryAlreadyExistsException extends DomainException {

    public CategoryAlreadyExistsException(String categoryName) {
        super("CATEGORY_ALREADY_EXISTS", "分類名稱已存在: " + categoryName);
    }
}
