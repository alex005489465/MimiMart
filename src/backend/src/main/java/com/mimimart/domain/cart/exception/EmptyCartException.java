package com.mimimart.domain.cart.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 購物車為空異常
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 2)
 */
public class EmptyCartException extends DomainException {

    public EmptyCartException() {
        super("EMPTY_CART", "購物車為空,無法建立訂單");
    }
}
