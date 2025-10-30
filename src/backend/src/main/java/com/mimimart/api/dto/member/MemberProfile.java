package com.mimimart.api.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 會員資料
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class MemberProfile {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String homeAddress;
    private Boolean emailVerified;
}
