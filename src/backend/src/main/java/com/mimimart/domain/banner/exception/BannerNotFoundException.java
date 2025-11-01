package com.mimimart.domain.banner.exception;

import com.mimimart.shared.exception.DomainException;

/**
 * 輪播圖不存在異常
 */
public class BannerNotFoundException extends DomainException {

    public BannerNotFoundException(Long bannerId) {
        super(String.format("輪播圖不存在: ID=%d", bannerId));
    }

    public BannerNotFoundException(String message) {
        super(message);
    }
}
