package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.banner.*;
import com.mimimart.application.service.BannerService;
import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mimimart.infrastructure.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 後台輪播圖管理 API
 */
@RestController
@RequestMapping("/api/admin/banner")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台輪播圖管理", description = "管理員輪播圖管理相關 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerService bannerService;
    private final com.mimimart.application.service.OpenAiImageService openAiImageService;
    private final com.mimimart.application.service.DeepseekService deepseekService;
    private final com.mimimart.infrastructure.storage.S3StorageService s3StorageService;
    private final com.mimimart.application.service.AiGenerationLogService aiGenerationLogService;

    /**
     * 查詢所有輪播圖 (含停用)
     */
    @GetMapping("/list")
    @Operation(summary = "查詢所有輪播圖", description = "查詢所有輪播圖 (包含停用),按顯示順序排序")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getAllBanners() {
        log.info("後台查詢所有輪播圖");

        List<BannerEntity> banners = bannerService.getAllBanners();
        List<BannerResponse> responses = banners.stream()
                .map(BannerResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", responses));
    }

    /**
     * 查詢單一輪播圖詳情
     */
    @GetMapping("/detail")
    @Operation(summary = "查詢輪播圖詳情", description = "根據 ID 查詢單一輪播圖詳情")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> getBannerDetail(
            @Parameter(description = "輪播圖 ID", required = true)
            @RequestParam Long bannerId) {
        log.info("後台查詢輪播圖詳情 - BannerId: {}", bannerId);

        BannerEntity banner = bannerService.getBannerById(bannerId);
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 建立輪播圖 (含圖片上傳)
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "建立輪播圖", description = "建立新的輪播圖並上傳圖片到 S3")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "建立成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "請求參數錯誤"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> createBanner(
            @Parameter(description = "輪播圖標題", required = true)
            @RequestParam String title,

            @Parameter(description = "圖片檔案", required = true)
            @RequestParam MultipartFile imageFile,

            @Parameter(description = "點擊連結 (可選)")
            @RequestParam(required = false) String linkUrl,

            @Parameter(description = "顯示順序", required = true)
            @RequestParam Integer displayOrder,

            @Parameter(description = "上架時間 (可選，格式：yyyy-MM-ddTHH:mm:ss，NULL 表示立即上架)")
            @RequestParam(required = false) LocalDateTime publishedAt,

            @Parameter(description = "下架時間 (可選，格式：yyyy-MM-ddTHH:mm:ss，NULL 表示永不下架)")
            @RequestParam(required = false) LocalDateTime unpublishedAt) {

        log.info("後台建立輪播圖 - Title: {}, DisplayOrder: {}, PublishedAt: {}, UnpublishedAt: {}",
                 title, displayOrder, publishedAt, unpublishedAt);

        // 驗證圖片檔案
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", "圖片檔案不能為空"));
        }

        BannerEntity banner = bannerService.createBanner(title, imageFile, linkUrl, displayOrder,
                                                         publishedAt, unpublishedAt);
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("建立成功", response));
    }

    /**
     * 更新輪播圖資訊 (不更新圖片)
     */
    @PostMapping("/update")
    @Operation(summary = "更新輪播圖資訊", description = "更新輪播圖資訊 (不更新圖片)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> updateBanner(
            @Valid @RequestBody UpdateBannerRequest request) {

        log.info("後台更新輪播圖 - BannerId: {}, PublishedAt: {}, UnpublishedAt: {}",
                 request.getBannerId(), request.getPublishedAt(), request.getUnpublishedAt());

        BannerEntity banner = bannerService.updateBanner(
                request.getBannerId(),
                request.getTitle(),
                request.getLinkUrl(),
                request.getDisplayOrder(),
                request.getPublishedAt(),
                request.getUnpublishedAt()
        );
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 更新輪播圖並替換圖片
     */
    @PostMapping(value = "/update-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新輪播圖並替換圖片", description = "更新輪播圖資訊並替換圖片")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> updateBannerWithImage(
            @Parameter(description = "輪播圖 ID", required = true)
            @RequestParam Long bannerId,

            @Parameter(description = "新圖片檔案", required = true)
            @RequestParam MultipartFile imageFile,

            @Parameter(description = "新標題 (可選)")
            @RequestParam(required = false) String title,

            @Parameter(description = "新連結 (可選)")
            @RequestParam(required = false) String linkUrl,

            @Parameter(description = "新顯示順序 (可選)")
            @RequestParam(required = false) Integer displayOrder,

            @Parameter(description = "上架時間 (可選，格式：yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) LocalDateTime publishedAt,

            @Parameter(description = "下架時間 (可選，格式：yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) LocalDateTime unpublishedAt) {

        log.info("後台更新輪播圖並替換圖片 - BannerId: {}, PublishedAt: {}, UnpublishedAt: {}",
                 bannerId, publishedAt, unpublishedAt);

        // 驗證圖片檔案
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", "圖片檔案不能為空"));
        }

        BannerEntity banner = bannerService.updateBannerWithImage(
                bannerId, title, imageFile, linkUrl, displayOrder, publishedAt, unpublishedAt
        );
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 刪除輪播圖
     */
    @PostMapping("/delete")
    @Operation(summary = "刪除輪播圖", description = "刪除輪播圖 (含 S3 圖片)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "刪除成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteBanner(
            @Valid @RequestBody BannerIdRequest request) {

        log.info("後台刪除輪播圖 - BannerId: {}", request.getBannerId());

        bannerService.deleteBanner(request.getBannerId());

        return ResponseEntity.ok(ApiResponse.success("刪除成功"));
    }

    /**
     * 啟用輪播圖
     */
    @PostMapping("/activate")
    @Operation(summary = "啟用輪播圖", description = "啟用輪播圖,使其顯示在前台")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "啟用成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> activateBanner(
            @Valid @RequestBody BannerIdRequest request) {

        log.info("後台啟用輪播圖 - BannerId: {}", request.getBannerId());

        BannerEntity banner = bannerService.activateBanner(request.getBannerId());
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("啟用成功", response));
    }

    /**
     * 停用輪播圖
     */
    @PostMapping("/deactivate")
    @Operation(summary = "停用輪播圖", description = "停用輪播圖,使其在前台隱藏")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "停用成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> deactivateBanner(
            @Valid @RequestBody BannerIdRequest request) {

        log.info("後台停用輪播圖 - BannerId: {}", request.getBannerId());

        BannerEntity banner = bannerService.deactivateBanner(request.getBannerId());
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("停用成功", response));
    }

    /**
     * 更新輪播圖顯示順序
     */
    @PostMapping("/update-order")
    @Operation(summary = "更新輪播圖順序", description = "更新輪播圖的顯示順序")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "順序更新成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "輪播圖不存在"
            )
    })
    public ResponseEntity<ApiResponse<BannerResponse>> updateBannerOrder(
            @Valid @RequestBody UpdateOrderRequest request) {

        log.info("後台更新輪播圖順序 - BannerId: {}, NewOrder: {}",
                request.getBannerId(), request.getDisplayOrder());

        BannerEntity banner = bannerService.updateBannerOrder(
                request.getBannerId(),
                request.getDisplayOrder()
        );
        BannerResponse response = BannerResponse.from(banner);

        return ResponseEntity.ok(ApiResponse.success("順序更新成功", response));
    }

    // ===== AI 輔助功能 =====

    /**
     * AI 生成輪播圖圖片
     */
    @PostMapping("/ai/generate-image")
    @Operation(summary = "AI 生成輪播圖", description = "使用 OpenAI DALL-E 生成輪播圖圖片")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "生成成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "生成失敗"
            )
    })
    public ResponseEntity<ApiResponse<AiImageResponse>> generateImage(
            @Valid @RequestBody AiImageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long adminId = userDetails.getUserId();

        log.info("AI 生成輪播圖 - AdminId: {}, Prompt: {}", adminId, request.prompt());

        String s3Key = openAiImageService.generateImage(
                request.prompt(),
                adminId,
                "/api/admin/banner/ai/generate-image"
        );

        AiImageResponse response = new AiImageResponse(s3Key);
        return ResponseEntity.ok(ApiResponse.success("圖片生成成功", response));
    }

    /**
     * AI 生成輪播圖描述文案
     */
    @PostMapping("/ai/generate-description")
    @Operation(summary = "AI 生成描述", description = "使用 Deepseek Chat 生成輪播圖文案")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "生成成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "生成失敗"
            )
    })
    public ResponseEntity<ApiResponse<AiDescriptionResponse>> generateDescription(
            @Valid @RequestBody AiDescriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long adminId = userDetails.getUserId();

        log.info("AI 生成描述 - AdminId: {}, Context: {}", adminId, request.context());

        String description = deepseekService.generateDescription(
                request.context(),
                adminId,
                "/api/admin/banner/ai/generate-description"
        );

        AiDescriptionResponse response = new AiDescriptionResponse(description);
        return ResponseEntity.ok(ApiResponse.success("描述生成成功", response));
    }

    /**
     * 下載 AI 生成的圖片
     */
    @GetMapping("/ai/download-image")
    @Operation(summary = "下載 AI 圖片", description = "下載 AI 生成的臨時圖片")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "下載成功"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "圖片不存在"
            )
    })
    public ResponseEntity<byte[]> downloadAiImage(@RequestParam String s3Key) {
        log.info("下載 AI 圖片 - S3 Key: {}", s3Key);

        byte[] imageData = s3StorageService.downloadAiImage(s3Key);
        String contentType = s3StorageService.getContentType(s3Key);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageData);
    }

    /**
     * 查詢 AI 生成歷史
     */
    @GetMapping("/ai/generation-history")
    @Operation(summary = "查詢 AI 生成歷史", description = "查詢當前管理員的 AI 生成歷史記錄")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<AiGenerationHistoryResponse>>> getGenerationHistory(
            @RequestParam(required = false) com.mimimart.shared.valueobject.GenerationType type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long adminId = userDetails.getUserId();

        log.info("查詢 AI 生成歷史 - AdminId: {}, Type: {}", adminId, type);

        List<com.mimimart.infrastructure.persistence.entity.AiGenerationLog> logs;
        if (type != null) {
            logs = aiGenerationLogService.getLogsByType(type, 0, 20).getContent();
        } else {
            logs = aiGenerationLogService.getLogsByAdmin(adminId);
        }

        List<AiGenerationHistoryResponse> response = logs.stream()
                .map(AiGenerationHistoryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    // ===== AI 請求/回應 DTO =====

    public record AiImageRequest(
            @Parameter(description = "圖片描述 prompt", required = true)
            String prompt
    ) {}

    public record AiImageResponse(
            @Parameter(description = "S3 圖片 Key")
            String s3Key
    ) {}

    public record AiDescriptionRequest(
            @Parameter(description = "上下文資訊", required = true)
            String context
    ) {}

    public record AiDescriptionResponse(
            @Parameter(description = "生成的描述")
            String description
    ) {}

    public record AiGenerationHistoryResponse(
            Long id,
            String apiEndpoint,
            com.mimimart.shared.valueobject.GenerationType generationType,
            com.mimimart.shared.valueobject.AiProvider aiProvider,
            String modelName,
            String prompt,
            String responseContent,
            String s3Key,
            Integer tokensUsed,
            java.math.BigDecimal costUsd,
            com.mimimart.shared.valueobject.AiGenerationStatus status,
            String errorMessage,
            java.time.LocalDateTime createdAt
    ) {
        public static AiGenerationHistoryResponse from(com.mimimart.infrastructure.persistence.entity.AiGenerationLog log) {
            return new AiGenerationHistoryResponse(
                    log.getId(),
                    log.getApiEndpoint(),
                    log.getGenerationType(),
                    log.getAiProvider(),
                    log.getModelName(),
                    log.getPrompt(),
                    log.getResponseContent(),
                    log.getS3Key(),
                    log.getTokensUsed(),
                    log.getCostUsd(),
                    log.getStatus(),
                    log.getErrorMessage(),
                    log.getCreatedAt()
            );
        }
    }
}
