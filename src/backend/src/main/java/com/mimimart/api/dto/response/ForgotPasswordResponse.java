package com.mimimart.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘記密碼回應
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    private String message;

    public static ForgotPasswordResponse success() {
        return new ForgotPasswordResponse("密碼重設郵件已發送，請查看您的信箱");
    }
}
