package com.mimimart.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 管理員資料
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class AdminProfile {
    private Long id;
    private String username;
    private String email;
    private String name;
}
