package com.mimimart.api.dto.member;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新會員資料請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "姓名長度不能超過 100 字元")
    private String name;

    @Size(max = 20, message = "電話長度不能超過 20 字元")
    private String phone;

    private String homeAddress;
}
