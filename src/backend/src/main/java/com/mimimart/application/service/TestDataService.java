package com.mimimart.application.service;

import com.mimimart.infrastructure.persistence.entity.Admin;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.AdminRepository;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.shared.valueobject.AdminStatus;
import com.mimimart.shared.valueobject.MemberStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 測試資料服務
 * 提供測試環境所需的測試帳號管理功能
 *
 * 測試帳號規格:
 * - Email: test-member-001@test.com ~ test-member-100@test.com
 * - Password: password123 (所有帳號統一)
 * - Name: 測試會員001 ~ 測試會員100
 *
 * 注意:此服務僅在開發/測試環境使用,生產環境應該禁用
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class TestDataService {

    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.test-endpoints.enabled:false}")
    private boolean testEndpointsEnabled;

    // 會員測試帳號常數
    private static final String EMAIL_TEMPLATE = "test-member-%03d@test.com";
    private static final String NAME_TEMPLATE = "測試會員%03d";
    private static final String PHONE_TEMPLATE = "0912345%03d";
    private static final String ADDRESS_TEMPLATE = "台北市信義區測試路%03d號";
    private static final String DEFAULT_PASSWORD = "password123";

    // 管理員測試帳號常數
    private static final String ADMIN_USERNAME_TEMPLATE = "test-admin-%03d";
    private static final String ADMIN_EMAIL_TEMPLATE = "test-admin-%03d@test.com";
    private static final String ADMIN_NAME_TEMPLATE = "測試管理員%03d";
    private static final String ADMIN_DEFAULT_PASSWORD = "admin123";

    // 通用常數
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 100;

    public TestDataService(MemberRepository memberRepository, AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 檢查測試端點是否啟用
     */
    private void checkTestEndpointsEnabled() {
        if (!testEndpointsEnabled) {
            throw new UnsupportedOperationException("測試端點已在此環境中停用");
        }
    }

    /**
     * 獲取或創建測試會員帳號
     *
     * @param count 帳號數量 (1-100)
     * @return 測試會員列表
     */
    @Transactional
    public List<Member> getOrCreateTestMembers(int count) {
        checkTestEndpointsEnabled();

        // 驗證數量範圍
        if (count < MIN_COUNT || count > MAX_COUNT) {
            throw new IllegalArgumentException(
                    String.format("帳號數量必須在 %d-%d 之間", MIN_COUNT, MAX_COUNT)
            );
        }

        List<Member> members = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String email = String.format(EMAIL_TEMPLATE, i);
            String name = String.format(NAME_TEMPLATE, i);

            // 檢查是否已存在
            Optional<Member> existingMember = memberRepository.findByEmail(email);

            if (existingMember.isPresent()) {
                members.add(existingMember.get());
                log.debug("測試帳號已存在: {}", email);
            } else {
                // 創建新的測試會員
                Member newMember = createTestMember(email, name, i);
                members.add(newMember);
                log.info("創建新的測試會員: {}", email);
            }
        }

        return members;
    }

    /**
     * 創建測試會員
     *
     * @param email Email
     * @param name  姓名
     * @param index 帳號編號 (1-100)
     * @return 會員實體
     */
    private Member createTestMember(String email, String name, int index) {
        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        member.setName(name);
        member.setPhone(String.format(PHONE_TEMPLATE, index));
        member.setHomeAddress(String.format(ADDRESS_TEMPLATE, index));
        member.setStatus(MemberStatus.ACTIVE);
        member.setEmailVerified(true); // 測試帳號預設已驗證

        // 不需要驗證 Token (測試帳號)
        member.setVerificationToken(null);
        member.setVerificationTokenExpiresAt(null);

        return memberRepository.save(member);
    }

    /**
     * 獲取預設密碼
     * (提供給 Controller 使用)
     *
     * @return 預設密碼
     */
    public String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    /**
     * 獲取或創建測試管理員帳號
     *
     * @param count 帳號數量 (1-100)
     * @return 測試管理員列表
     */
    @Transactional
    public List<Admin> getOrCreateTestAdmins(int count) {
        checkTestEndpointsEnabled();

        // 驗證數量範圍
        if (count < MIN_COUNT || count > MAX_COUNT) {
            throw new IllegalArgumentException(
                    String.format("帳號數量必須在 %d-%d 之間", MIN_COUNT, MAX_COUNT)
            );
        }

        List<Admin> admins = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String username = String.format(ADMIN_USERNAME_TEMPLATE, i);
            String email = String.format(ADMIN_EMAIL_TEMPLATE, i);
            String name = String.format(ADMIN_NAME_TEMPLATE, i);

            // 檢查是否已存在（透過 username 查詢）
            Optional<Admin> existingAdmin = adminRepository.findByUsername(username);

            if (existingAdmin.isPresent()) {
                admins.add(existingAdmin.get());
                log.debug("測試管理員帳號已存在: {}", username);
            } else {
                // 創建新的測試管理員
                Admin newAdmin = createTestAdmin(username, email, name);
                admins.add(newAdmin);
                log.info("創建新的測試管理員: {}", username);
            }
        }

        return admins;
    }

    /**
     * 創建測試管理員
     *
     * @param username 帳號
     * @param email    Email
     * @param name     姓名
     * @return 管理員實體
     */
    private Admin createTestAdmin(String username, String email, String name) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_DEFAULT_PASSWORD));
        admin.setName(name);
        admin.setStatus(AdminStatus.ACTIVE);

        return adminRepository.save(admin);
    }

    /**
     * 獲取管理員預設密碼
     * (提供給 Controller 使用)
     *
     * @return 管理員預設密碼
     */
    public String getAdminDefaultPassword() {
        return ADMIN_DEFAULT_PASSWORD;
    }
}
