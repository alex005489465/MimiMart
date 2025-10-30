package com.mimimart.api.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 會員註冊請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 50, message = "密碼長度必須在 6-50 字元之間")
    private String password;

    @NotBlank(message = "姓名不能為空")
    @Size(max = 100, message = "姓名長度不能超過 100 字元")
    private String name;
}
