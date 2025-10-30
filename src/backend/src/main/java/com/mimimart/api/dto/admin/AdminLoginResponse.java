package com.mimimart.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 管理員登入回應
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class AdminLoginResponse {
    private String accessToken;
    private String refreshToken;
    private AdminProfile profile;
}
