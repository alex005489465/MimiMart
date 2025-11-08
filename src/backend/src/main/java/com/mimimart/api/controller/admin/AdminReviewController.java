package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.review.AdminReplyRequest;
import com.mimimart.api.dto.review.ReviewIdRequest;
import com.mimimart.api.dto.review.ReviewResponse;
import com.mimimart.application.service.ReviewService;
import com.mimimart.domain.review.model.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 後台評價管理 Controller
 */
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
@Tag(name = "後台 - 評價管理", description = "後台評價管理 API")
public class AdminReviewController {

    private final ReviewService reviewService;

    /**
     * 查詢評價列表（分頁）
     */
    @GetMapping("/list")
    @Operation(summary = "查詢評價列表", description = "查詢所有評價，支援分頁和排序")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewList(
            @Parameter(description = "頁碼（從1開始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（asc/desc）") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // 轉換為 0-based 分頁
        int zeroBasedPage = page - 1;
        Sort sort = Sort.by(
            sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
            sortBy
        );
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);

        Page<Review> reviewPage = reviewService.getAllReviews(pageable);

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        // 建立分頁 metadata（轉換為 1-based）
        Map<String, Object> meta = new HashMap<>();
        meta.put("currentPage", reviewPage.getNumber() + 1);
        meta.put("totalPages", reviewPage.getTotalPages());
        meta.put("totalItems", reviewPage.getTotalElements());
        meta.put("pageSize", reviewPage.getSize());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", reviews, meta));
    }

    /**
     * 查詢評價詳情
     */
    @GetMapping("/detail")
    @Operation(summary = "查詢評價詳情", description = "根據評價ID查詢詳細資訊")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewDetail(
            @Parameter(description = "評價ID") @RequestParam Long reviewId
    ) {
        // 查詢評價（使用 getAllReviews 的邏輯，但這裡直接使用 reviewId）
        // 暫時使用分頁查詢，實際應該有專用的查詢方法
        Pageable pageable = PageRequest.of(0, 1);
        Page<Review> reviewPage = reviewService.getAllReviews(pageable);

        // TODO: 應該實作專用的 getReviewById 方法
        // 目前暫時從列表中查找
        Review review = reviewPage.getContent().stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("評價不存在"));

        return ResponseEntity.ok(ApiResponse.success("查詢成功", ReviewResponse.from(review)));
    }

    /**
     * 管理員回覆評價
     */
    @PostMapping("/reply")
    @Operation(summary = "回覆評價", description = "管理員對評價進行回覆")
    public ResponseEntity<ApiResponse<ReviewResponse>> replyReview(
            @Valid @RequestBody AdminReplyRequest request
    ) {
        Review review = reviewService.replyReview(
                request.getReviewId(),
                request.getReplyContent()
        );

        return ResponseEntity.ok(ApiResponse.success("回覆成功", ReviewResponse.from(review)));
    }

    /**
     * 隱藏評價
     */
    @PostMapping("/hide")
    @Operation(summary = "隱藏評價", description = "將評價設為不可見（前台不顯示）")
    public ResponseEntity<ApiResponse<ReviewResponse>> hideReview(
            @Valid @RequestBody ReviewIdRequest request
    ) {
        Review review = reviewService.hideReview(request.getReviewId());
        return ResponseEntity.ok(ApiResponse.success("評價已隱藏", ReviewResponse.from(review)));
    }

    /**
     * 顯示評價
     */
    @PostMapping("/show")
    @Operation(summary = "顯示評價", description = "將評價設為可見（前台顯示）")
    public ResponseEntity<ApiResponse<ReviewResponse>> showReview(
            @Valid @RequestBody ReviewIdRequest request
    ) {
        Review review = reviewService.showReview(request.getReviewId());
        return ResponseEntity.ok(ApiResponse.success("評價已顯示", ReviewResponse.from(review)));
    }
}
