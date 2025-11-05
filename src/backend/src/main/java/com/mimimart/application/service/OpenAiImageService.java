package com.mimimart.application.service;

import com.mimimart.infrastructure.storage.S3StorageService;
import com.mimimart.shared.valueobject.AiProvider;
import com.mimimart.shared.valueobject.GenerationType;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.ImageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;

/**
 * OpenAI DALL-E 圖片生成服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class OpenAiImageService {

    private final com.theokanning.openai.service.OpenAiService openAiClient;
    private final S3StorageService s3StorageService;
    private final AiGenerationLogService aiGenerationLogService;
    private final String model;
    private final String imageSize;
    private final String imageQuality;

    public OpenAiImageService(
            @Value("${mimimart.ai.openai.api-key}") String apiKey,
            @Value("${mimimart.ai.openai.api-url:https://api.openai.com/v1}") String apiUrl,
            @Value("${mimimart.ai.openai.model}") String model,
            @Value("${mimimart.ai.openai.image-size}") String imageSize,
            @Value("${mimimart.ai.openai.image-quality}") String imageQuality,
            S3StorageService s3StorageService,
            AiGenerationLogService aiGenerationLogService) {
        // 注意：SDK 會自動在 base URL 後添加具體端點（如 /images/generations）
        this.openAiClient = new com.theokanning.openai.service.OpenAiService(apiKey, Duration.ofSeconds(60));
        this.model = model;
        this.imageSize = imageSize;
        this.imageQuality = imageQuality;
        this.s3StorageService = s3StorageService;
        this.aiGenerationLogService = aiGenerationLogService;
        log.info("OpenAiImageService 已初始化 - API URL: {}, Model: {}, ImageSize: {}, Quality: {}",
                apiUrl, model, imageSize, imageQuality);
    }

    /**
     * 生成圖片
     *
     * @param prompt      圖片描述
     * @param adminId     管理員 ID
     * @param apiEndpoint 調用的 API 端點
     * @return S3 Key
     */
    public String generateImage(String prompt, Long adminId, String apiEndpoint) {
        log.info("開始生成圖片 - AdminId: {}, Prompt: {}", adminId, prompt);

        try {
            // 建立 DALL-E 請求
            CreateImageRequest.CreateImageRequestBuilder requestBuilder = CreateImageRequest.builder()
                    .prompt(prompt)
                    .size(imageSize)
                    .n(1)  // 生成 1 張圖片
                    .responseFormat("url");  // 返回 URL

            // 只有 DALL-E 3 才明確指定 model 和 quality
            // DALL-E 2 使用默認設置（某些 SDK 版本不支持為 DALL-E 2 指定 model 參數）
            if ("dall-e-3".equals(model)) {
                requestBuilder.model(model);
                requestBuilder.quality(imageQuality);
            }

            CreateImageRequest request = requestBuilder.build();

            // 調用 OpenAI API
            ImageResult result = openAiClient.createImage(request);
            String imageUrl = result.getData().get(0).getUrl();
            log.info("OpenAI 圖片生成成功 - URL: {}", imageUrl);

            // 下載圖片
            byte[] imageData = downloadImageFromUrl(imageUrl);
            log.info("圖片下載成功 - Size: {} bytes", imageData.length);

            // 上傳到 S3
            String s3Key = s3StorageService.uploadAiImage(imageData, "image/png", ".png");
            log.info("圖片上傳到 S3 成功 - S3 Key: {}", s3Key);

            // 記錄日誌 (估算成本)
            BigDecimal cost = calculateCost(imageSize, imageQuality);
            aiGenerationLogService.logSuccess(
                    adminId,
                    apiEndpoint,
                    GenerationType.IMAGE,
                    AiProvider.OPENAI,
                    model,
                    prompt,
                    imageUrl,  // 回應內容為 OpenAI 的臨時 URL
                    s3Key,
                    null,  // DALL-E 不回傳 token 用量
                    cost
            );

            return s3Key;

        } catch (Exception e) {
            log.error("生成圖片失敗 - AdminId: {}, Error: {}", adminId, e.getMessage(), e);

            // 記錄失敗日誌
            aiGenerationLogService.logFailure(
                    adminId,
                    apiEndpoint,
                    GenerationType.IMAGE,
                    AiProvider.OPENAI,
                    model,
                    prompt,
                    e.getMessage()
            );

            throw new RuntimeException("AI 圖片生成失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 從 URL 下載圖片
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * 估算圖片生成成本 (USD)
     * 參考: https://openai.com/pricing
     */
    private BigDecimal calculateCost(String size, String quality) {
        // DALL-E 3 定價 (2024)
        if ("dall-e-3".equals(model)) {
            if ("hd".equals(quality)) {
                // HD 品質
                return switch (size) {
                    case "1024x1024" -> new BigDecimal("0.080");
                    case "1024x1792", "1792x1024" -> new BigDecimal("0.120");
                    default -> new BigDecimal("0.080");
                };
            } else {
                // Standard 品質
                return switch (size) {
                    case "1024x1024" -> new BigDecimal("0.040");
                    case "1024x1792", "1792x1024" -> new BigDecimal("0.080");
                    default -> new BigDecimal("0.040");
                };
            }
        }

        // DALL-E 2 定價
        if ("dall-e-2".equals(model)) {
            return switch (size) {
                case "1024x1024" -> new BigDecimal("0.020");
                case "512x512" -> new BigDecimal("0.018");
                case "256x256" -> new BigDecimal("0.016");
                default -> new BigDecimal("0.020");
            };
        }

        return BigDecimal.ZERO;
    }
}
