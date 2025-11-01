package com.mimimart.domain.banner.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 無效的輪播圖順序異常
 */
public class InvalidBannerOrderException extends DomainException {

    public InvalidBannerOrderException(Integer order) {
        super(String.format("無效的顯示順序: %d (必須大於或等於 0)", order));
    }

    public InvalidBannerOrderException(String message) {
        super(message);
    }
}
