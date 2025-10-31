package com.mimimart.api.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 頭貼上傳回應
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class AvatarUploadResponse {
    private String avatarUrl;
    private LocalDateTime avatarUpdatedAt;
}
