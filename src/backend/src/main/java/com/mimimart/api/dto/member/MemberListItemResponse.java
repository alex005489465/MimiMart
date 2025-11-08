package com.mimimart.api.dto.member;

import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.shared.valueobject.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 會員列表項目回應 DTO
 * 用於管理後台會員列表顯示（簡化資料）
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListItemResponse {

    /**
     * 會員 ID
     */
    private Long id;

    /**
     * Email
     */
    private String email;

    /**
     * 姓名
     */
    private String name;

    /**
     * 電話
     */
    private String phone;

    /**
     * 帳號狀態
     */
    private MemberStatus status;

    /**
     * 帳號狀態顯示名稱
     */
    private String statusDisplayName;

    /**
     * Email 是否已驗證
     */
    private Boolean emailVerified;

    /**
     * 註冊時間
     */
    private LocalDateTime createdAt;

    /**
     * 最後登入時間
     */
    private LocalDateTime lastLoginAt;

    /**
     * 從 Member Entity 轉換
     */
    public static MemberListItemResponse from(Member member) {
        return new MemberListItemResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                getStatusDisplayName(member.getStatus()),
                member.getEmailVerified(),
                member.getCreatedAt(),
                member.getLastLoginAt()
        );
    }

    /**
     * 取得狀態顯示名稱
     */
    private static String getStatusDisplayName(MemberStatus status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case ACTIVE:
                return "啟用";
            case DISABLED:
                return "停用";
            case BANNED:
                return "封禁";
            default:
                return "未知";
        }
    }
}
