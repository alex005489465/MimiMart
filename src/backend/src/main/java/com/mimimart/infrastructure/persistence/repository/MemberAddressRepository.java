package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 會員地址 Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    /**
     * 查詢會員的所有地址
     */
    List<MemberAddress> findByMemberId(Long memberId);

    /**
     * 查詢會員的預設地址
     */
    Optional<MemberAddress> findByMemberIdAndIsDefaultTrue(Long memberId);

    /**
     * 查詢會員的特定地址
     */
    Optional<MemberAddress> findByIdAndMemberId(Long id, Long memberId);
}
