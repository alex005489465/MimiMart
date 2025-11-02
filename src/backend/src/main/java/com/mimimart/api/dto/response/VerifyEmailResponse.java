package com.mimimart.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email 驗證回應
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailResponse {

    private String message;
    private boolean verified;

    public static VerifyEmailResponse success() {
        return new VerifyEmailResponse("Email 驗證成功", true);
    }
}
