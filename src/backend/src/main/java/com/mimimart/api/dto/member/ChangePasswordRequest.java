package com.mimimart.api.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密碼請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "舊密碼不能為空")
    private String oldPassword;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, max = 50, message = "新密碼長度必須在 6-50 字元之間")
    private String newPassword;
}
