package com.mimimart.application.service;

import com.mimimart.domain.member.exception.MemberNotFoundException;
import com.mimimart.domain.product.exception.ProductNotFoundException;
import com.mimimart.domain.review.exception.ReviewAlreadyExistsException;
import com.mimimart.domain.review.exception.ReviewNotFoundException;
import com.mimimart.domain.review.exception.UnauthorizedReviewException;
import com.mimimart.domain.review.model.Rating;
import com.mimimart.domain.review.model.Review;
import com.mimimart.domain.review.service.ReviewEligibilityService;
import com.mimimart.infrastructure.persistence.entity.ProductReview;
import com.mimimart.infrastructure.persistence.mapper.ReviewMapper;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import com.mimimart.infrastructure.persistence.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 評價應用服務
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ReviewEligibilityService eligibilityService;
    private final ReviewMapper reviewMapper;

    /**
     * 前台：新增評價
     * 包含完整的應用層驗證
     *
     * @param memberId 會員ID
     * @param orderItemId 訂單項目ID
     * @param productId 商品ID
     * @param ratingValue 評分值（1-5）
     * @param content 評價內容
     * @return 建立的評價
     */
    @Transactional
    public Review createReview(Long memberId, Long orderItemId, Long productId, int ratingValue, String content) {
        // 1. 完整外鍵存在性驗證
        productRepository.findByIdAndIsDeletedFalse(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("會員不存在: ID=" + memberId));

        // 2. 評價資格驗證（含訂單項目存在性、擁有權、送達狀態、期限）
        eligibilityService.validateReviewEligibility(memberId, orderItemId);

        // 3. 唯一性驗證（應用層約束）
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new ReviewAlreadyExistsException(orderItemId);
        }

        // 4. Rating 值對象驗證（Domain 層第二道防線）
        Rating rating = Rating.of(ratingValue);

        // 5. 建立評價領域模型
        Review review = Review.builder()
                .productId(productId)
                .memberId(memberId)
                .orderItemId(orderItemId)
                .rating(rating)
                .content(content)
                .build();

        // 6. 持久化
        ProductReview entity = reviewMapper.toEntity(review);
        ProductReview saved = reviewRepository.save(entity);

        return reviewMapper.toDomain(saved);
    }

    /**
     * 前台：更新評價
     *
     * @param memberId 會員ID
     * @param reviewId 評價ID
     * @param ratingValue 新評分
     * @param content 新評價內容
     * @return 更新後的評價
     */
    @Transactional
    public Review updateReview(Long memberId, Long reviewId, Integer ratingValue, String content) {
        // 1. 查詢評價
        ProductReview entity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 2. 驗證擁有權
        if (!entity.getMemberId().equals(memberId)) {
            throw new UnauthorizedReviewException("無權修改此評價");
        }

        // 3. 轉換為領域模型並更新
        Review review = reviewMapper.toDomain(entity);
        Rating newRating = ratingValue != null ? Rating.of(ratingValue) : null;
        review.update(newRating, content);

        // 4. 持久化
        ProductReview updated = reviewMapper.toEntity(review);
        ProductReview saved = reviewRepository.save(updated);

        return reviewMapper.toDomain(saved);
    }

    /**
     * 前台：刪除評價
     *
     * @param memberId 會員ID
     * @param reviewId 評價ID
     */
    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        // 1. 查詢評價
        ProductReview entity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 2. 驗證擁有權
        if (!entity.getMemberId().equals(memberId)) {
            throw new UnauthorizedReviewException("無權刪除此評價");
        }

        // 3. 刪除（物理刪除）
        reviewRepository.delete(entity);
    }

    /**
     * 前台：查詢商品評價列表（分頁）
     *
     * @param productId 商品ID
     * @param pageable 分頁參數
     * @return 評價分頁列表
     */
    public Page<Review> getProductReviews(Long productId, Pageable pageable) {
        Page<ProductReview> entities = reviewRepository.findByProductIdAndIsVisibleTrue(productId, pageable);
        return entities.map(reviewMapper::toDomain);
    }

    /**
     * 前台：查詢會員的評價列表
     *
     * @param memberId 會員ID
     * @param pageable 分頁參數
     * @return 評價分頁列表
     */
    public Page<Review> getMemberReviews(Long memberId, Pageable pageable) {
        Page<ProductReview> entities = reviewRepository.findByMemberId(memberId, pageable);
        return entities.map(reviewMapper::toDomain);
    }

    /**
     * 前台：取得商品評價統計
     *
     * @param productId 商品ID
     * @return Map 包含 totalReviews 和 avgRating
     */
    public Map<String, Object> getReviewStats(Long productId) {
        Object[] stats = reviewRepository.getReviewStats(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("totalReviews", ((Number) stats[0]).longValue());
        result.put("avgRating", ((Number) stats[1]).doubleValue());

        return result;
    }

    /**
     * 前台：取得商品評分分布
     *
     * @param productId 商品ID
     * @return Map<評分, 數量>
     */
    public Map<Integer, Long> getRatingDistribution(Long productId) {
        List<Object[]> distribution = reviewRepository.getRatingDistribution(productId);

        Map<Integer, Long> result = new HashMap<>();
        // 初始化所有評分為 0
        for (int i = 1; i <= 5; i++) {
            result.put(i, 0L);
        }

        // 填入實際數據
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(rating, count);
        }

        return result;
    }

    /**
     * 後台：管理員回覆評價
     *
     * @param reviewId 評價ID
     * @param replyContent 回覆內容
     * @return 更新後的評價
     */
    @Transactional
    public Review replyReview(Long reviewId, String replyContent) {
        // 1. 查詢評價
        ProductReview entity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 2. 轉換為領域模型並回覆
        Review review = reviewMapper.toDomain(entity);
        review.reply(replyContent);

        // 3. 持久化
        ProductReview updated = reviewMapper.toEntity(review);
        ProductReview saved = reviewRepository.save(updated);

        return reviewMapper.toDomain(saved);
    }

    /**
     * 後台：隱藏評價
     *
     * @param reviewId 評價ID
     * @return 更新後的評價
     */
    @Transactional
    public Review hideReview(Long reviewId) {
        ProductReview entity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        Review review = reviewMapper.toDomain(entity);
        review.hide();

        ProductReview updated = reviewMapper.toEntity(review);
        ProductReview saved = reviewRepository.save(updated);

        return reviewMapper.toDomain(saved);
    }

    /**
     * 後台：顯示評價
     *
     * @param reviewId 評價ID
     * @return 更新後的評價
     */
    @Transactional
    public Review showReview(Long reviewId) {
        ProductReview entity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        Review review = reviewMapper.toDomain(entity);
        review.show();

        ProductReview updated = reviewMapper.toEntity(review);
        ProductReview saved = reviewRepository.save(updated);

        return reviewMapper.toDomain(saved);
    }

    /**
     * 後台：查詢所有評價（支援動態篩選）
     *
     * @param pageable 分頁參數
     * @return 評價分頁列表
     */
    public Page<Review> getAllReviews(Pageable pageable) {
        Page<ProductReview> entities = reviewRepository.findAll(pageable);
        return entities.map(reviewMapper::toDomain);
    }

    /**
     * 檢查評價資格（用於前端顯示）
     *
     * @param memberId 會員ID
     * @param orderItemId 訂單項目ID
     * @return 是否符合評價資格
     */
    public boolean checkEligibility(Long memberId, Long orderItemId) {
        // 先檢查是否已評價
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            return false;
        }

        return eligibilityService.isEligibleForReview(memberId, orderItemId);
    }
}
