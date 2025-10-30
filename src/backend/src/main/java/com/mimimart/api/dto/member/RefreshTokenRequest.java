package com.mimimart.api.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh Token 請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token 不能為空")
    private String refreshToken;
}
