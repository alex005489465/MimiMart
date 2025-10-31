package com.mimimart.api.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查看頭貼請求
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarViewRequest {
    @NotNull(message = "會員 ID 不能為空")
    private Long memberId;
}
