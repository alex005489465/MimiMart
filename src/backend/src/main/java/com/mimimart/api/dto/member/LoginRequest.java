package com.mimimart.api.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 會員登入請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    private String password;
}
