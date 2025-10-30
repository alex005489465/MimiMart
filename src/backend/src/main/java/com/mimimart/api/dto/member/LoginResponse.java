package com.mimimart.api.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 會員登入回應
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private MemberProfile profile;
}
