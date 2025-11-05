package com.mimimart.infrastructure.persistence.entity;

import com.mimimart.shared.valueobject.AiGenerationStatus;
import com.mimimart.shared.valueobject.AiProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 生成調用日誌 Entity
 * 對應 ai_generation_log 資料表
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "ai_generation_log", indexes = {
    @Index(name = "idx_admin_id", columnList = "admin_id"),
    @Index(name = "idx_api_endpoint", columnList = "api_endpoint"),
    @Index(name = "idx_type_provider", columnList = "generation_type, ai_provider"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationLog {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "api_endpoint", nullable = false, length = 200)
    private String apiEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_type", nullable = false, length = 20)
    private com.mimimart.shared.valueobject.GenerationType generationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_provider", nullable = false, length = 20)
    private AiProvider aiProvider;

    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "cost_usd", precision = 10, scale = 6)
    private BigDecimal costUsd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiGenerationStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
