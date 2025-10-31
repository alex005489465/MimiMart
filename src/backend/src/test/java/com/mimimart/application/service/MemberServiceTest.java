package com.mimimart.application.service;

import com.mimimart.domain.member.exception.InvalidCredentialsException;
import com.mimimart.domain.member.exception.MemberNotFoundException;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MemberService 測試類別
 * 測試會員資料管理相關功能
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@DisplayName("會員資料服務測試")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 建立測試用會員
        testMember = authService.register("member@example.com", "password123", "測試會員");
    }

    @Test
    @DisplayName("查詢個人資料 - 成功取得會員資訊")
    void testGetProfile_Success() {
        // When
        Member profile = memberService.getProfile(testMember.getId());

        // Then
        assertNotNull(profile);
        assertEquals(testMember.getId(), profile.getId());
        assertEquals(testMember.getEmail(), profile.getEmail());
        assertEquals(testMember.getName(), profile.getName());
    }

    @Test
    @DisplayName("查詢個人資料 - 會員不存在應拋出例外")
    void testGetProfile_MemberNotFound() {
        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            memberService.getProfile(99999L);
        });
    }

    @Test
    @DisplayName("更新個人資料 - 成功更新姓名、電話、地址")
    void testUpdateProfile_Success() {
        // Given
        String newName = "新名字";
        String newPhone = "0912345678";
        String newAddress = "台北市信義區";

        // When
        Member updatedMember = memberService.updateProfile(
                testMember.getId(),
                newName,
                newPhone,
                newAddress
        );

        // Then
        assertNotNull(updatedMember);
        assertEquals(newName, updatedMember.getName());
        assertEquals(newPhone, updatedMember.getPhone());
        assertEquals(newAddress, updatedMember.getHomeAddress());

        // 驗證資料庫中的資料也已更新
        Member dbMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertEquals(newName, dbMember.getName());
        assertEquals(newPhone, dbMember.getPhone());
        assertEquals(newAddress, dbMember.getHomeAddress());
    }

    @Test
    @DisplayName("更新個人資料 - 部分更新(僅更新姓名)")
    void testUpdateProfile_PartialUpdate() {
        // Given
        String newName = "只改名字";

        // When
        Member updatedMember = memberService.updateProfile(
                testMember.getId(),
                newName,
                null,
                null
        );

        // Then
        assertEquals(newName, updatedMember.getName());
        assertNull(updatedMember.getPhone()); // 電話應該保持不變
        assertNull(updatedMember.getHomeAddress()); // 地址應該保持不變
    }

    @Test
    @DisplayName("更新個人資料 - 會員不存在應拋出例外")
    void testUpdateProfile_MemberNotFound() {
        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            memberService.updateProfile(99999L, "新名字", null, null);
        });
    }

    @Test
    @DisplayName("修改密碼 - 使用正確的舊密碼成功修改")
    void testChangePassword_Success() {
        // Given
        String oldPassword = "password123";
        String newPassword = "newpassword456";

        // When
        memberService.changePassword(testMember.getId(), oldPassword, newPassword);

        // Then
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, updatedMember.getPasswordHash()));
        assertFalse(passwordEncoder.matches(oldPassword, updatedMember.getPasswordHash()));
    }

    @Test
    @DisplayName("修改密碼 - 舊密碼錯誤應拋出例外")
    void testChangePassword_WrongOldPassword() {
        // Given
        String wrongOldPassword = "wrongpassword";
        String newPassword = "newpassword456";

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            memberService.changePassword(testMember.getId(), wrongOldPassword, newPassword);
        });
    }

    @Test
    @DisplayName("修改密碼 - 會員不存在應拋出例外")
    void testChangePassword_MemberNotFound() {
        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            memberService.changePassword(99999L, "oldpassword", "newpassword");
        });
    }
}
