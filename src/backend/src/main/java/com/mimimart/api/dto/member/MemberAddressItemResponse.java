package com.mimimart.api.dto.member;

import com.mimimart.infrastructure.persistence.entity.MemberAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 會員地址項目回應 DTO
 * 用於管理後台顯示會員的收貨地址列表
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberAddressItemResponse {

    /**
     * 地址 ID
     */
    private Long id;

    /**
     * 收件人姓名
     */
    private String recipientName;

    /**
     * 收件人電話
     */
    private String recipientPhone;

    /**
     * 收貨地址
     */
    private String address;

    /**
     * 是否為預設地址
     */
    private Boolean isDefault;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 從 MemberAddress Entity 轉換
     */
    public static MemberAddressItemResponse from(MemberAddress address) {
        return new MemberAddressItemResponse(
                address.getId(),
                address.getRecipientName(),
                address.getPhone(),
                address.getAddress(),
                address.getIsDefault(),
                address.getCreatedAt()
        );
    }
}
