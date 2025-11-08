package com.mimimart.api.dto.member;

import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.shared.valueobject.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 會員詳細資料回應 DTO
 * 用於管理後台顯示會員完整資料
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResponse {

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
     * 住家地址
     */
    private String homeAddress;

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
     * 頭貼 URL
     */
    private String avatarUrl;

    /**
     * 頭貼更新時間
     */
    private LocalDateTime avatarUpdatedAt;

    /**
     * 註冊時間
     */
    private LocalDateTime createdAt;

    /**
     * 最後登入時間
     */
    private LocalDateTime lastLoginAt;

    /**
     * 收貨地址列表
     */
    private List<MemberAddressItemResponse> addresses;

    /**
     * 從 Member Entity 轉換（不含地址列表）
     */
    public static MemberDetailResponse from(Member member) {
        return new MemberDetailResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getHomeAddress(),
                member.getStatus(),
                getStatusDisplayName(member.getStatus()),
                member.getEmailVerified(),
                member.getAvatarUrl(),
                member.getAvatarUpdatedAt(),
                member.getCreatedAt(),
                member.getLastLoginAt(),
                null  // 地址列表需要額外設定
        );
    }

    /**
     * 從 Member Entity 轉換（含地址列表）
     */
    public static MemberDetailResponse from(Member member, List<MemberAddressItemResponse> addresses) {
        MemberDetailResponse response = from(member);
        response.addresses = addresses;
        return response;
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
