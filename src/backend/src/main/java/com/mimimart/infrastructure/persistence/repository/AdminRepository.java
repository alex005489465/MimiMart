package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理員 Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * 根據 Email 查詢管理員
     */
    Optional<Admin> findByEmail(String email);

    /**
     * 根據 Username 查詢管理員
     */
    Optional<Admin> findByUsername(String username);

    /**
     * 檢查 Email 是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 檢查 Username 是否已存在
     */
    boolean existsByUsername(String username);
}
