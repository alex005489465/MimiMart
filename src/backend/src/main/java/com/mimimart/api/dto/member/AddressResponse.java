package com.mimimart.api.dto.member;

import com.mimimart.infrastructure.persistence.entity.MemberAddress;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地址回應
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String recipientName;
    private String phone;
    private String address;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressResponse from(MemberAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getPhone(),
                address.getAddress(),
                address.getIsDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
