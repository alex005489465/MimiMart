package com.mimimart.domain.cart.exception;

/**
 * 無效數量異常
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
public class InvalidQuantityException extends RuntimeException {

    public InvalidQuantityException(String message) {
        super(message);
    }

    public static InvalidQuantityException tooSmall(int minQuantity) {
        return new InvalidQuantityException("數量不可小於 " + minQuantity);
    }

    public static InvalidQuantityException tooLarge(int maxQuantity) {
        return new InvalidQuantityException("數量不可大於 " + maxQuantity);
    }

    public static InvalidQuantityException exceedsLimit(int newQuantity, int maxQuantity) {
        return new InvalidQuantityException(
            String.format("累加後數量 %d 超過上限 %d", newQuantity, maxQuantity)
        );
    }
}
