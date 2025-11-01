package com.mimimart.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 測試管理員帳號回應 DTO
 * 用於返回測試環境的管理員帳號資訊
 */
@Data
@AllArgsConstructor
public class TestAdminAccountResponse {
    /**
     * 管理員帳號（主要登入用）
     */
    private String username;

    /**
     * 管理員信箱
     */
    private String email;

    /**
     * 預設密碼（明文，僅供測試使用）
     */
    private String password;
}
