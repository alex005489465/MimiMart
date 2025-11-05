package com.mimimart.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.shared.valueobject.AiProvider;
import com.mimimart.shared.valueobject.GenerationType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * Deepseek Chat 文字生成服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class DeepseekService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AiGenerationLogService aiGenerationLogService;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public DeepseekService(
            @Value("${mimimart.ai.deepseek.api-key}") String apiKey,
            @Value("${mimimart.ai.deepseek.api-url}") String apiUrl,
            @Value("${mimimart.ai.deepseek.model}") String model,
            AiGenerationLogService aiGenerationLogService) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.aiGenerationLogService = aiGenerationLogService;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
        log.info("DeepseekService 已初始化 - API URL: {}, Model: {}", apiUrl, model);
    }

    /**
     * 生成文字描述
     *
     * @param context     上下文或提示
     * @param adminId     管理員 ID
     * @param apiEndpoint 調用的 API 端點
     * @return 生成的文字
     */
    public String generateDescription(String context, Long adminId, String apiEndpoint) {
        log.info("開始生成描述 - AdminId: {}, Context: {}", adminId, context);

        String systemPrompt = "你是一個專業的電商文案撰寫專家，擅長撰寫吸引人的輪播圖標題和描述。" +
                "請根據提供的內容，生成簡潔有力、吸引消費者的輪播圖文案。文案應該突出產品特色，並包含號召行動。";

        String userPrompt = "請為以下內容生成一段適合電商輪播圖的文案（50字以內）：\n" + context;

        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        try {
            // 建立請求 JSON
            String requestJson = String.format("""
                {
                    "model": "%s",
                    "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"}
                    ],
                    "temperature": 0.7,
                    "max_tokens": 200
                }
                """, model, escapeJson(systemPrompt), escapeJson(userPrompt));

            // 建立 HTTP 請求
            Request request = new Request.Builder()
                    .url(apiUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                    .build();

            // 發送請求
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    throw new IOException("Deepseek API 調用失敗: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                log.info("Deepseek API 回應成功");

                // 解析回應
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String generatedText = jsonNode
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                // 取得 token 用量
                JsonNode usageNode = jsonNode.path("usage");
                int tokensUsed = usageNode.path("total_tokens").asInt(0);

                // 計算成本
                BigDecimal cost = calculateCost(tokensUsed);

                // 記錄日誌
                aiGenerationLogService.logSuccess(
                        adminId,
                        apiEndpoint,
                        GenerationType.TEXT,
                        AiProvider.DEEPSEEK,
                        model,
                        userPrompt,
                        generatedText,
                        null,  // 文字生成不需要 S3 Key
                        tokensUsed,
                        cost
                );

                log.info("描述生成成功 - Tokens: {}, Cost: ${}", tokensUsed, cost);
                return generatedText.trim();
            }

        } catch (Exception e) {
            log.error("生成描述失敗 - AdminId: {}, Error: {}", adminId, e.getMessage(), e);

            // 記錄失敗日誌
            aiGenerationLogService.logFailure(
                    adminId,
                    apiEndpoint,
                    GenerationType.TEXT,
                    AiProvider.DEEPSEEK,
                    model,
                    fullPrompt,
                    e.getMessage()
            );

            throw new RuntimeException("AI 描述生成失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 轉義 JSON 字串
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 估算成本 (USD)
     * Deepseek Chat 定價: https://platform.deepseek.com/pricing
     * - Input: $0.14 / 1M tokens
     * - Output: $0.28 / 1M tokens
     * - 簡化計算: 平均 $0.21 / 1M tokens
     */
    private BigDecimal calculateCost(int tokensUsed) {
        BigDecimal pricePerMillionTokens = new BigDecimal("0.21");
        return pricePerMillionTokens
                .multiply(BigDecimal.valueOf(tokensUsed))
                .divide(BigDecimal.valueOf(1_000_000), 6, BigDecimal.ROUND_HALF_UP);
    }
}
