package com.mimimart.application.service;

import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.test-endpoints.enabled:false}")
    private boolean testEndpointsEnabled;

    // 測試帳號常數
    private static final String EMAIL_TEMPLATE = "test-member-%03d@test.com";
    private static final String NAME_TEMPLATE = "測試會員%03d";
    private static final String PHONE_TEMPLATE = "0912345%03d";
    private static final String ADDRESS_TEMPLATE = "台北市信義區測試路%03d號";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 100;

    public TestDataService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
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
}
