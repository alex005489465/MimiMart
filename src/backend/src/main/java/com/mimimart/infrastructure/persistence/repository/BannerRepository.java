package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 輪播圖 Repository
 */
@Repository
public interface BannerRepository extends JpaRepository<BannerEntity, Long> {

    /**
     * 查詢指定狀態的輪播圖,按顯示順序排序
     *
     * @param status 輪播圖狀態
     * @return 輪播圖列表
     */
    List<BannerEntity> findByStatusOrderByDisplayOrderAsc(BannerStatus status);

    /**
     * 查詢所有輪播圖,按顯示順序排序
     *
     * @return 輪播圖列表
     */
    List<BannerEntity> findAllByOrderByDisplayOrderAsc();

    /**
     * 查詢已上架且未下架的輪播圖,按顯示順序排序
     * 條件：
     * 1. status = ACTIVE（已啟用）
     * 2. publishedAt IS NULL OR publishedAt <= now（已到上架時間）
     * 3. unpublishedAt IS NULL OR unpublishedAt > now（未到下架時間）
     *
     * @param status 輪播圖狀態
     * @param now    當前時間
     * @return 已上架且未下架的輪播圖列表
     */
    @Query("SELECT b FROM BannerEntity b WHERE b.status = :status " +
           "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
           "AND (b.unpublishedAt IS NULL OR b.unpublishedAt > :now) " +
           "ORDER BY b.displayOrder ASC")
    List<BannerEntity> findPublishedBanners(@Param("status") BannerStatus status,
                                            @Param("now") LocalDateTime now);
}
