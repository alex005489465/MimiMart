package com.mimimart.api.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 管理員回覆評價請求 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminReplyRequest {

    @NotNull(message = "評價ID不得為空")
    private Long reviewId;

    @NotBlank(message = "回覆內容不得為空")
    @Size(max = 1000, message = "回覆內容不得超過1000字")
    private String replyContent;
}
