package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 會員 Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 根據 Email 查詢會員
     */
    Optional<Member> findByEmail(String email);

    /**
     * 根據驗證 Token 查詢會員
     */
    Optional<Member> findByVerificationToken(String token);

    /**
     * 根據密碼重設 Token 查詢會員
     */
    Optional<Member> findByPasswordResetToken(String token);

    /**
     * 檢查 Email 是否已存在
     */
    boolean existsByEmail(String email);
}
