package com.mimimart.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 創建測試帳號請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "創建測試帳號請求")
public class CreateTestAccountRequest {

    @NotBlank(message = "帳號類型不能為空")
    @Pattern(regexp = "^(member|admin)$", message = "帳號類型必須是 member 或 admin")
    @Schema(description = "帳號類型", example = "member", allowableValues = {"member", "admin"})
    private String accountType;

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "Email 地址", example = "test@example.com")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在 6-100 之間")
    @Schema(description = "密碼", example = "password123")
    private String password;

    @Schema(description = "用戶名（僅管理員需要）", example = "testadmin")
    private String username;

    @Schema(description = "姓名", example = "測試用戶")
    private String name;

    @Schema(description = "電話（僅會員）", example = "0912345678")
    private String phone;

    @Schema(description = "地址（僅會員）", example = "台北市信義區測試路1號")
    private String homeAddress;
}
