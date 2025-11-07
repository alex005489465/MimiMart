package com.mimimart.fixtures;

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
     * 取得預設密碼
     *
     * @return 預設密碼
     */
    public String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }
}
