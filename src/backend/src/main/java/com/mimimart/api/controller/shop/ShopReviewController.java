package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.review.*;
import com.mimimart.application.service.ReviewService;
import com.mimimart.domain.review.model.Review;
import com.mimimart.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前台評價 Controller
 */
@Tag(name = "前台評價管理", description = "商品評價相關 API")
@RestController
@RequestMapping("/api/shop/review")
@RequiredArgsConstructor
public class ShopReviewController {

    private final ReviewService reviewService;

    /**
     * 新增評價
     */
    @Operation(summary = "新增評價", description = "會員對已購買商品進行評價（送達後7天內）")
    @PostMapping("/create")
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        Review review = reviewService.createReview(
                userDetails.getUserId(),
                request.getOrderItemId(),
                request.getProductId(),
                request.getRating(),
                request.getContent()
        );

        return ApiResponse.success("評價新增成功", ReviewResponse.from(review));
    }

    /**
     * 更新評價
     */
    @Operation(summary = "更新評價", description = "修改自己的評價內容或評分")
    @PostMapping("/update")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        Review review = reviewService.updateReview(
                userDetails.getUserId(),
                request.getReviewId(),
                request.getRating(),
                request.getContent()
        );

        return ApiResponse.success("評價更新成功", ReviewResponse.from(review));
    }

    /**
     * 刪除評價
     */
    @Operation(summary = "刪除評價", description = "刪除自己的評價")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewIdRequest request
    ) {
        reviewService.deleteReview(userDetails.getUserId(), request.getReviewId());
        return ApiResponse.success("評價刪除成功");
    }

    /**
     * 查詢商品評價列表（分頁）
     */
    @Operation(summary = "查詢商品評價列表", description = "查詢指定商品的所有可見評價")
    @GetMapping("/list")
    public ApiResponse<List<ReviewResponse>> getProductReviews(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // 轉換為 0-based 分頁
        int zeroBasedPage = page - 1;
        Sort sort = Sort.by(
            sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
            sortBy
        );
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);

        Page<Review> reviewPage = reviewService.getProductReviews(productId, pageable);

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        // 建立分頁 metadata（轉換為 1-based）
        Map<String, Object> meta = new HashMap<>();
        meta.put("currentPage", reviewPage.getNumber() + 1);
        meta.put("totalPages", reviewPage.getTotalPages());
        meta.put("totalItems", reviewPage.getTotalElements());
        meta.put("pageSize", reviewPage.getSize());

        return ApiResponse.success("查詢成功", reviews, meta);
    }

    /**
     * 查詢商品評價統計
     */
    @Operation(summary = "查詢商品評價統計", description = "取得商品的平均評分、總評價數和評分分布")
    @GetMapping("/stats")
    public ApiResponse<ReviewStatsResponse> getReviewStats(@RequestParam Long productId) {
        Map<String, Object> stats = reviewService.getReviewStats(productId);
        Map<Integer, Long> distribution = reviewService.getRatingDistribution(productId);

        ReviewStatsResponse response = ReviewStatsResponse.create(
                (Long) stats.get("totalReviews"),
                (Double) stats.get("avgRating"),
                distribution
        );

        return ApiResponse.success("查詢成功", response);
    }

    /**
     * 查詢會員的評價列表
     */
    @Operation(summary = "查詢我的評價列表", description = "查詢當前會員的所有評價")
    @GetMapping("/my-reviews")
    public ApiResponse<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviewPage = reviewService.getMemberReviews(userDetails.getUserId(), pageable);

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        Map<String, Object> meta = new HashMap<>();
        meta.put("currentPage", reviewPage.getNumber() + 1);
        meta.put("totalPages", reviewPage.getTotalPages());
        meta.put("totalItems", reviewPage.getTotalElements());
        meta.put("pageSize", reviewPage.getSize());

        return ApiResponse.success("查詢成功", reviews, meta);
    }

    /**
     * 檢查評價資格
     */
    @Operation(summary = "檢查評價資格", description = "檢查是否可對指定訂單項目進行評價")
    @GetMapping("/eligibility")
    public ApiResponse<ReviewEligibilityResponse> checkEligibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderItemId
    ) {
        boolean canReview = reviewService.checkEligibility(userDetails.getUserId(), orderItemId);

        ReviewEligibilityResponse response = new ReviewEligibilityResponse(
                canReview,
                canReview ? null : "不符合評價資格（可能已評價、未購買或超過期限）"
        );

        return ApiResponse.success("查詢成功", response);
    }
}
