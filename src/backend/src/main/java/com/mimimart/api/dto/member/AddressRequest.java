package com.mimimart.api.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 地址請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
public class AddressRequest {

    @NotBlank(message = "收件人姓名不能為空")
    @Size(max = 100, message = "收件人姓名長度不能超過 100 字元")
    private String recipientName;

    @NotBlank(message = "電話不能為空")
    @Size(max = 20, message = "電話長度不能超過 20 字元")
    private String phone;

    @NotBlank(message = "地址不能為空")
    private String address;

    private Boolean isDefault;
}
