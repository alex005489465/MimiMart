package com.mimimart.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 創建測試帳號響應
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "創建測試帳號響應")
public class CreateTestAccountResponse {

    @Schema(description = "帳號類型", example = "member")
    private String accountType;

    @Schema(description = "Email 地址", example = "test@example.com")
    private String email;

    @Schema(description = "用戶名（管理員才有）", example = "testadmin")
    private String username;

    @Schema(description = "姓名", example = "測試用戶")
    private String name;

    @Schema(description = "操作結果", example = "created", allowableValues = {"created", "updated"})
    private String action;

    public static CreateTestAccountResponse forMember(String email, String name, String action) {
        return new CreateTestAccountResponse("member", email, null, name, action);
    }

    public static CreateTestAccountResponse forAdmin(String username, String email, String name, String action) {
        return new CreateTestAccountResponse("admin", email, username, name, action);
    }
}
