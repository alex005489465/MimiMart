package com.mimimart.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重設密碼回應
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {

    private String message;
    private boolean success;

    public static ResetPasswordResponse success() {
        return new ResetPasswordResponse("密碼重設成功", true);
    }
}
