package com.mimimart.api.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 地址 ID 請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressIdRequest {

    @NotNull(message = "地址 ID 不能為空")
    private Long addressId;
}
