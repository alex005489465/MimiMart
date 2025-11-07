package com.mimimart.application.service;

import com.mimimart.infrastructure.persistence.entity.AiGenerationLog;
import com.mimimart.infrastructure.persistence.repository.AiGenerationLogRepository;
import com.mimimart.shared.valueobject.AiGenerationStatus;
import com.mimimart.shared.valueobject.AiProvider;
import com.mimimart.shared.valueobject.GenerationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 生成日誌服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiGenerationLogService {

    private final AiGenerationLogRepository aiGenerationLogRepository;

    /**
     * 記錄 AI 生成調用 (成功)
     */
    @Transactional
    public AiGenerationLog logSuccess(
            Long adminId,
            String apiEndpoint,
            GenerationType generationType,
            AiProvider aiProvider,
            String modelName,
            String prompt,
            String responseContent,
            String s3Key,
            Integer tokensUsed,
            BigDecimal costUsd) {

        AiGenerationLog entity = new AiGenerationLog();
        entity.setAdminId(adminId);
        entity.setApiEndpoint(apiEndpoint);
        entity.setGenerationType(generationType);
        entity.setAiProvider(aiProvider);
        entity.setModelName(modelName);
        entity.setPrompt(prompt);
        entity.setResponseContent(responseContent);
        entity.setS3Key(s3Key);
        entity.setTokensUsed(tokensUsed);
        entity.setCostUsd(costUsd);
        entity.setStatus(AiGenerationStatus.SUCCESS);
        entity.setCreatedAt(LocalDateTime.now());

        AiGenerationLog saved = aiGenerationLogRepository.save(entity);
        log.info("AI 生成調用記錄成功 - LogId: {}, AdminId: {}, Type: {}, Provider: {}",
                saved.getId(), adminId, generationType, aiProvider);
        return saved;
    }

    /**
     * 記錄 AI 生成調用 (失敗)
     */
    @Transactional
    public AiGenerationLog logFailure(
            Long adminId,
            String apiEndpoint,
            GenerationType generationType,
            AiProvider aiProvider,
            String modelName,
            String prompt,
            String errorMessage) {

        AiGenerationLog entity = new AiGenerationLog();
        entity.setAdminId(adminId);
        entity.setApiEndpoint(apiEndpoint);
        entity.setGenerationType(generationType);
        entity.setAiProvider(aiProvider);
        entity.setModelName(modelName);
        entity.setPrompt(prompt);
        entity.setStatus(AiGenerationStatus.FAILED);
        entity.setErrorMessage(errorMessage);
        entity.setCreatedAt(LocalDateTime.now());

        AiGenerationLog saved = aiGenerationLogRepository.save(entity);
        log.warn("AI 生成調用失敗 - LogId: {}, AdminId: {}, Error: {}",
                saved.getId(), adminId, errorMessage);
        return saved;
    }

    /**
     * 查詢指定管理員的 AI 生成記錄
     */
    public List<AiGenerationLog> getLogsByAdmin(Long adminId) {
        return aiGenerationLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
    }

    /**
     * 查詢指定類型的 AI 生成記錄（分頁）
     * @param generationType 生成類型
     * @param page 頁碼 (從 1 開始)
     * @param size 每頁筆數
     */
    public Page<AiGenerationLog> getLogsByType(GenerationType generationType, int page, int size) {
        // 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size);
        return aiGenerationLogRepository.findByGenerationTypeOrderByCreatedAtDesc(generationType, pageable);
    }

    /**
     * 查詢指定 API 端點的調用記錄
     */
    public List<AiGenerationLog> getLogsByEndpoint(String apiEndpoint) {
        return aiGenerationLogRepository.findByApiEndpointOrderByCreatedAtDesc(apiEndpoint);
    }

    /**
     * 查詢指定時間範圍內的記錄
     */
    public List<AiGenerationLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return aiGenerationLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startTime, endTime);
    }

    /**
     * 統計指定管理員的調用次數
     */
    public long countByAdmin(Long adminId) {
        return aiGenerationLogRepository.countByAdminId(adminId);
    }

    /**
     * 統計指定類型和提供商的調用次數
     */
    public long countByTypeAndProvider(GenerationType generationType, AiProvider aiProvider) {
        return aiGenerationLogRepository.countByGenerationTypeAndAiProvider(generationType, aiProvider);
    }
}
