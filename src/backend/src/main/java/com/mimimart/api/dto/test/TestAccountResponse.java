package com.mimimart.api.dto.test;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 測試帳號回應
 * 用於提供前端測試用的帳號資訊
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class TestAccountResponse {
    /**
     * 測試帳號 Email
     */
    private String email;

    /**
     * 測試帳號密碼 (明文,僅測試環境使用)
     */
    private String password;
}
