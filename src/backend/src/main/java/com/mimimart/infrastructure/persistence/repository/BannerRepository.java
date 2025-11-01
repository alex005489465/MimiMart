package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
