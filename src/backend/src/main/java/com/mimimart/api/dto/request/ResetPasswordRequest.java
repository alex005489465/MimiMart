package com.mimimart.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重設密碼請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "重設 Token 不能為空")
    private String token;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在 6-100 之間")
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;
}
