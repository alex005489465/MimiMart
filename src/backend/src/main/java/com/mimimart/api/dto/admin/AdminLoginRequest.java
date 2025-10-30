package com.mimimart.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理員登入請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class AdminLoginRequest {

    @NotBlank(message = "帳號不能為空")
    private String username;

    @NotBlank(message = "密碼不能為空")
    private String password;
}
