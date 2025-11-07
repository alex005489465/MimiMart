package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.banner.BannerResponse;
import com.mimimart.application.service.BannerService;
import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台輪播圖 API
 */
@RestController
@RequestMapping("/api/shop/banner")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台輪播圖", description = "前台輪播圖相關 API (公開)")
public class ShopBannerController {

    private final BannerService bannerService;

    /**
     * 查詢啟用且已上架的輪播圖 (公開端點)
     * 過濾條件：
     * 1. status = ACTIVE（已啟用）
     * 2. publishedAt IS NULL OR publishedAt <= now（已到上架時間）
     * 3. unpublishedAt IS NULL OR unpublishedAt > now（未到下架時間）
     */
    @GetMapping("/list")
    @Operation(summary = "查詢輪播圖",
               description = "查詢所有啟用且已上架的輪播圖，自動過濾未上架和已下架的內容，按顯示順序排序")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getActiveBanners() {
        log.info("前台查詢啟用且已上架的輪播圖");

        List<BannerEntity> banners = bannerService.getActiveBanners();
        List<BannerResponse> responses = banners.stream()
                .map(BannerResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", responses));
    }
}
