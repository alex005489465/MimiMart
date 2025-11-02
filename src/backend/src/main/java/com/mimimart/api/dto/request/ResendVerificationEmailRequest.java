package com.mimimart.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重新發送驗證郵件請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationEmailRequest {

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;
}
