package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.EmailQuotaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 郵件配額配置 Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface EmailQuotaConfigRepository extends JpaRepository<EmailQuotaConfig, Long> {

    /**
     * 根據配置鍵查詢配置
     *
     * @param configKey 配置鍵
     * @return 配置資料
     */
    Optional<EmailQuotaConfig> findByConfigKey(String configKey);
}
