package com.mimimart.domain.cart.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 商品庫存不足異常
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public class InsufficientStockException extends DomainException {

    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableStock;

    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableStock) {
        super(String.format("商品 ID %d 庫存不足：請求數量 %d，可用庫存 %d",
                productId, requestedQuantity, availableStock));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}
