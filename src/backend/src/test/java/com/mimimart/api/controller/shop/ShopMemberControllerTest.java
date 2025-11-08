package com.mimimart.api.controller.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.api.dto.member.ChangePasswordRequest;
import com.mimimart.api.dto.member.UpdateProfileRequest;
import com.mimimart.application.service.AuthService;
import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.security.CustomUserDetails;
import com.mimimart.shared.valueobject.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ShopMemberController 測試類別
 * 測試會員資料管理 API 端點
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("會員資料管理 API 測試")
class ShopMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestFixtures fixtures;

    private Member testMember;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 建立測試用會員（使用 fixtures）
        testMember = fixtures.createTestMember(1);

        // 建立 UserDetails 用於認證
        userDetails = new CustomUserDetails(
                testMember.getId(),
                testMember.getEmail(),
                testMember.getPasswordHash(),
                Collections.emptyList(),
                UserType.MEMBER
        );
    }

    @Test
    @DisplayName("GET /api/shop/member/profile - 成功查看個人資料")
    void testGetProfile_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/shop/member/profile")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查詢成功"))
                .andExpect(jsonPath("$.data.id").value(testMember.getId()))
                .andExpect(jsonPath("$.data.email").value(testMember.getEmail()))
                .andExpect(jsonPath("$.data.name").value(testMember.getName()))
                .andExpect(jsonPath("$.data.emailVerified").value(true)); // fixtures 建立的會員已驗證
    }

    @Test
    @DisplayName("POST /api/shop/member/profile/update - 成功更新個人資料")
    void testUpdateProfile_Success() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("新名字");
        request.setPhone("0912345678");
        request.setHomeAddress("台北市信義區");

        // When & Then
        mockMvc.perform(post("/api/shop/member/profile/update")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.name").value("新名字"))
                .andExpect(jsonPath("$.data.phone").value("0912345678"))
                .andExpect(jsonPath("$.data.homeAddress").value("台北市信義區"));

        // 驗證資料庫中的資料已更新
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertTrue(updatedMember.getName().equals("新名字"));
        assertTrue(updatedMember.getPhone().equals("0912345678"));
        assertTrue(updatedMember.getHomeAddress().equals("台北市信義區"));
    }

    @Test
    @DisplayName("POST /api/shop/member/profile/update - 部分更新個人資料")
    void testUpdateProfile_PartialUpdate() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("只改名字");

        // When & Then
        mockMvc.perform(post("/api/shop/member/profile/update")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.name").value("只改名字"));
    }

    @Test
    @DisplayName("POST /api/shop/member/change-password - 成功修改密碼")
    void testChangePassword_Success() throws Exception {
        // Given - fixtures 的預設密碼是 "fixture123"
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(fixtures.getDefaultPassword());
        request.setNewPassword("newpassword456");

        // When & Then
        mockMvc.perform(post("/api/shop/member/change-password")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密碼修改成功"));

        // 驗證密碼已更新
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newpassword456", updatedMember.getPasswordHash()));
    }

    @Test
    @DisplayName("POST /api/shop/member/change-password - 舊密碼錯誤應返回失敗")
    void testChangePassword_WrongOldPassword() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword456");

        // When & Then
        mockMvc.perform(post("/api/shop/member/change-password")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("舊密碼錯誤"));
    }
}
