package com.mimimart.fixtures;

import com.mimimart.application.service.AuthService;
import com.mimimart.infrastructure.persistence.entity.Category;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.persistence.repository.CategoryRepository;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import com.mimimart.shared.valueobject.MemberStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 測試資料輔助類
 * 提供購物車測試所需的測試資料建立方法
 *
 * 命名策略:
 * - 會員: cart-test-{timestamp}-{index}@fixture.test
 * - 分類: 測試分類-{timestamp}-{index}
 * - 商品: 測試商品-{timestamp}-{index}
 *
 * 特點:
 * - 使用時間戳確保每次測試資料唯一
 * - 與 TestDataService 命名風格明顯區別
 * - 透過 @Transactional 測試完自動回滾
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@Component
public class TestFixtures {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private static final String DEFAULT_PASSWORD = "fixture123";

    /**
     * 建立測試會員
     *
     * @param index 會員編號
     * @return 測試會員實體
     */
    public Member createTestMember(int index) {
        long timestamp = System.currentTimeMillis();
        String email = String.format("cart-test-%d-%03d@fixture.test", timestamp, index);
        String name = String.format("購物車測試會員%03d", index);

        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        member.setName(name);
        member.setPhone(String.format("0900%06d", index));
        member.setHomeAddress(String.format("測試地址%03d號", index));
        member.setStatus(MemberStatus.ACTIVE);
        member.setEmailVerified(true);
        member.setVerificationToken(null);
        member.setVerificationTokenExpiresAt(null);

        return memberRepository.save(member);
    }

    /**
     * 建立測試分類
     *
     * @param index 分類編號
     * @return 測試分類實體
     */
    public Category createTestCategory(int index) {
        long timestamp = System.currentTimeMillis();
        String name = String.format("測試分類-%d-%03d", timestamp, index);

        Category category = new Category();
        category.setName(name);
        category.setDescription("購物車測試用分類");
        category.setSortOrder(index);

        return categoryRepository.save(category);
    }

    /**
     * 建立測試商品
     *
     * @param categoryId 分類 ID
     * @param index 商品編號
     * @return 測試商品實體
     */
    public Product createTestProduct(Long categoryId, int index) {
        long timestamp = System.currentTimeMillis();
        String name = String.format("測試商品-%d-%03d", timestamp, index);

        Product product = new Product();
        product.setName(name);
        product.setDescription("購物車測試用商品");
        product.setPrice(BigDecimal.valueOf(99 + index * 100));
        product.setCategoryId(categoryId);
        product.setIsPublished(true);
        product.setIsDeleted(false);
        product.setImageUrl(String.format("/test/product-%03d.jpg", index));

        return productRepository.save(product);
    }

    /**
     * 建立測試會員並返回 LoginResult（含 Access Token 和 Refresh Token）
     * 適用於需要驗證完整註冊+登入流程的測試
     *
     * @param index 會員編號
     * @return AuthService.LoginResult（含會員資料與 Token）
     */
    public AuthService.LoginResult createTestMemberWithAuth(int index) {
        long timestamp = System.currentTimeMillis();
        String email = String.format("auth-test-%d-%03d@fixture.test", timestamp, index);
        String name = String.format("認證測試會員%03d", index);

        return authService.register(email, DEFAULT_PASSWORD, name);
    }

    /**
     * 建立未驗證的測試會員（含 verificationToken）
     * 適用於 Email 驗證流程測試
     *
     * @param index 會員編號
     * @return 測試會員實體（含 verificationToken）
     */
    public Member createTestMemberForEmailVerification(int index) {
        long timestamp = System.currentTimeMillis();
        String email = String.format("verify-test-%d-%03d@fixture.test", timestamp, index);
        String name = String.format("驗證測試會員%03d", index);

        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        member.setName(name);
        member.setPhone(String.format("0900%06d", index));
        member.setHomeAddress(String.format("測試地址%03d號", index));
        member.setStatus(MemberStatus.ACTIVE);
        member.setEmailVerified(false);

        // 設定驗證 Token（24 小時有效期）
        member.setVerificationToken(UUID.randomUUID().toString());
        member.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));

        return memberRepository.save(member);
    }

    /**
     * 建立測試會員（含 passwordResetToken）
     * 適用於密碼重設流程測試
     *
     * @param index 會員編號
     * @return 測試會員實體（含 passwordResetToken）
     */
    public Member createTestMemberForPasswordReset(int index) {
        long timestamp = System.currentTimeMillis();
        String email = String.format("reset-test-%d-%03d@fixture.test", timestamp, index);
        String name = String.format("重設測試會員%03d", index);

        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        member.setName(name);
        member.setPhone(String.format("0900%06d", index));
        member.setHomeAddress(String.format("測試地址%03d號", index));
        member.setStatus(MemberStatus.ACTIVE);
        member.setEmailVerified(true);

        // 設定重設密碼 Token（30 分鐘有效期）
        member.setPasswordResetToken(UUID.randomUUID().toString());
        member.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));

        return memberRepository.save(member);
    }

    /**
     * 取得預設密碼
     *
     * @return 預設密碼
     */
    public String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }
}
