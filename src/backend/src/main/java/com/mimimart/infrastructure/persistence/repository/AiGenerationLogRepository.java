package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.AiGenerationLog;
import com.mimimart.shared.valueobject.AiProvider;
import com.mimimart.shared.valueobject.GenerationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 生成日誌 Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLog, Long> {

    /**
     * 查詢指定管理員的 AI 生成記錄
     */
    List<AiGenerationLog> findByAdminIdOrderByCreatedAtDesc(Long adminId);

    /**
     * 查詢指定類型的 AI 生成記錄（分頁）
     */
    Page<AiGenerationLog> findByGenerationTypeOrderByCreatedAtDesc(GenerationType generationType, Pageable pageable);

    /**
     * 查詢指定 API 端點的調用記錄
     */
    List<AiGenerationLog> findByApiEndpointOrderByCreatedAtDesc(String apiEndpoint);

    /**
     * 查詢指定時間範圍內的記錄
     */
    List<AiGenerationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 統計指定管理員的調用次數
     */
    @Query("SELECT COUNT(a) FROM AiGenerationLog a WHERE a.adminId = :adminId")
    long countByAdminId(@Param("adminId") Long adminId);

    /**
     * 統計指定類型和提供商的調用次數
     */
    @Query("SELECT COUNT(a) FROM AiGenerationLog a WHERE a.generationType = :type AND a.aiProvider = :provider")
    long countByGenerationTypeAndAiProvider(
        @Param("type") GenerationType generationType,
        @Param("provider") AiProvider aiProvider
    );
}
