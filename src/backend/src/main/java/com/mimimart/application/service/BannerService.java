package com.mimimart.application.service;

import com.mimimart.domain.banner.exception.BannerNotFoundException;
import com.mimimart.domain.banner.exception.InvalidBannerOrderException;
import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import com.mimimart.infrastructure.persistence.repository.BannerRepository;
import com.mimimart.infrastructure.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 輪播圖應用服務
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {

    private final BannerRepository bannerRepository;
    private final S3StorageService s3StorageService;

    /**
     * 查詢所有啟用的輪播圖 (前台使用)
     * 過濾條件：
     * 1. status = ACTIVE
     * 2. 已到上架時間 (publishedAt IS NULL OR publishedAt <= now)
     * 3. 未到下架時間 (unpublishedAt IS NULL OR unpublishedAt > now)
     *
     * @return 啟用且在上架期間的輪播圖列表,按顯示順序排序
     */
    @Transactional(readOnly = true)
    public List<BannerEntity> getActiveBanners() {
        log.info("查詢所有啟用且已上架的輪播圖");
        return bannerRepository.findPublishedBanners(BannerStatus.ACTIVE, LocalDateTime.now());
    }

    /**
     * 查詢所有輪播圖 (後台使用)
     *
     * @return 所有輪播圖列表,按顯示順序排序
     */
    @Transactional(readOnly = true)
    public List<BannerEntity> getAllBanners() {
        log.info("查詢所有輪播圖");
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 根據 ID 查詢輪播圖
     *
     * @param bannerId 輪播圖 ID
     * @return 輪播圖實體
     * @throws BannerNotFoundException 當輪播圖不存在時
     */
    @Transactional(readOnly = true)
    public BannerEntity getBannerById(Long bannerId) {
        log.info("查詢輪播圖 - BannerId: {}", bannerId);
        return bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BannerNotFoundException(bannerId));
    }

    /**
     * 建立輪播圖 (含圖片上傳)
     *
     * @param title         輪播圖標題
     * @param imageFile     圖片檔案
     * @param linkUrl       點擊連結 (可為 null)
     * @param displayOrder  顯示順序
     * @param publishedAt   上架時間 (可為 null，表示立即上架)
     * @param unpublishedAt 下架時間 (可為 null，表示永不下架)
     * @return 建立的輪播圖實體
     */
    @Transactional
    public BannerEntity createBanner(String title, MultipartFile imageFile, String linkUrl,
                                     Integer displayOrder, LocalDateTime publishedAt,
                                     LocalDateTime unpublishedAt) {
        log.info("建立輪播圖 - Title: {}, DisplayOrder: {}, PublishedAt: {}, UnpublishedAt: {}",
                 title, displayOrder, publishedAt, unpublishedAt);

        // 驗證顯示順序
        validateDisplayOrder(displayOrder);

        // 驗證上架/下架時間
        validatePublishSchedule(publishedAt, unpublishedAt);

        // 上傳圖片到 S3
        String imageUrl = s3StorageService.uploadBanner(imageFile);

        // 建立輪播圖實體
        BannerEntity banner = new BannerEntity();
        banner.setTitle(title);
        banner.setImageUrl(imageUrl);
        banner.setLinkUrl(linkUrl);
        banner.setDisplayOrder(displayOrder);
        banner.setPublishedAt(publishedAt);
        banner.setUnpublishedAt(unpublishedAt);
        banner.setStatus(BannerStatus.ACTIVE); // 預設啟用

        BannerEntity savedBanner = bannerRepository.save(banner);
        log.info("輪播圖建立成功 - BannerId: {}, ImageUrl: {}", savedBanner.getId(), imageUrl);

        return savedBanner;
    }

    /**
     * 更新輪播圖資訊 (不更新圖片)
     *
     * @param bannerId      輪播圖 ID
     * @param title         新標題 (可為 null 表示不更新)
     * @param linkUrl       新連結 (可為 null)
     * @param displayOrder  新順序 (可為 null 表示不更新)
     * @param publishedAt   上架時間 (可為 null)
     * @param unpublishedAt 下架時間 (可為 null)
     * @return 更新後的輪播圖實體
     * @throws BannerNotFoundException 當輪播圖不存在時
     */
    @Transactional
    public BannerEntity updateBanner(Long bannerId, String title, String linkUrl,
                                     Integer displayOrder, LocalDateTime publishedAt,
                                     LocalDateTime unpublishedAt) {
        log.info("更新輪播圖 - BannerId: {}, Title: {}, DisplayOrder: {}, PublishedAt: {}, UnpublishedAt: {}",
                 bannerId, title, displayOrder, publishedAt, unpublishedAt);

        // 驗證顯示順序
        if (displayOrder != null) {
            validateDisplayOrder(displayOrder);
        }

        // 驗證上架/下架時間
        validatePublishSchedule(publishedAt, unpublishedAt);

        // 查詢現有輪播圖
        BannerEntity banner = getBannerById(bannerId);

        // 更新資訊
        banner.updateInfo(title, linkUrl, displayOrder);
        banner.setPublishedAt(publishedAt);
        banner.setUnpublishedAt(unpublishedAt);

        BannerEntity updatedBanner = bannerRepository.save(banner);
        log.info("輪播圖更新成功 - BannerId: {}", bannerId);

        return updatedBanner;
    }

    /**
     * 更新輪播圖並替換圖片
     *
     * @param bannerId      輪播圖 ID
     * @param title         新標題 (可為 null 表示不更新)
     * @param imageFile     新圖片檔案
     * @param linkUrl       新連結 (可為 null)
     * @param displayOrder  新順序 (可為 null 表示不更新)
     * @param publishedAt   上架時間 (可為 null)
     * @param unpublishedAt 下架時間 (可為 null)
     * @return 更新後的輪播圖實體
     * @throws BannerNotFoundException 當輪播圖不存在時
     */
    @Transactional
    public BannerEntity updateBannerWithImage(Long bannerId, String title, MultipartFile imageFile,
                                              String linkUrl, Integer displayOrder,
                                              LocalDateTime publishedAt, LocalDateTime unpublishedAt) {
        log.info("更新輪播圖並替換圖片 - BannerId: {}, PublishedAt: {}, UnpublishedAt: {}",
                 bannerId, publishedAt, unpublishedAt);

        // 驗證顯示順序
        if (displayOrder != null) {
            validateDisplayOrder(displayOrder);
        }

        // 驗證上架/下架時間
        validatePublishSchedule(publishedAt, unpublishedAt);

        // 查詢現有輪播圖
        BannerEntity banner = getBannerById(bannerId);
        String oldImageUrl = banner.getImageUrl();

        // 上傳新圖片
        String newImageUrl = s3StorageService.uploadBanner(imageFile);

        // 更新輪播圖資訊和圖片
        banner.updateInfo(title, linkUrl, displayOrder);
        banner.updateImageUrl(newImageUrl);
        banner.setPublishedAt(publishedAt);
        banner.setUnpublishedAt(unpublishedAt);

        BannerEntity updatedBanner = bannerRepository.save(banner);

        // 刪除舊圖片
        try {
            s3StorageService.deleteBanner(oldImageUrl);
            log.info("舊圖片刪除成功 - OldImageUrl: {}", oldImageUrl);
        } catch (Exception e) {
            log.warn("舊圖片刪除失敗 (不影響更新操作) - OldImageUrl: {}, Error: {}", oldImageUrl, e.getMessage());
        }

        log.info("輪播圖更新成功 (含圖片替換) - BannerId: {}, NewImageUrl: {}", bannerId, newImageUrl);
        return updatedBanner;
    }

    /**
     * 刪除輪播圖 (含刪除 S3 圖片)
     *
     * @param bannerId 輪播圖 ID
     * @throws BannerNotFoundException 當輪播圖不存在時
     */
    @Transactional
    public void deleteBanner(Long bannerId) {
        log.info("刪除輪播圖 - BannerId: {}", bannerId);

        // 查詢現有輪播圖
        BannerEntity banner = getBannerById(bannerId);
        String imageUrl = banner.getImageUrl();

        // 刪除資料庫記錄
        bannerRepository.delete(banner);

        // 刪除 S3 圖片
        try {
            s3StorageService.deleteBanner(imageUrl);
            log.info("輪播圖刪除成功 - BannerId: {}, ImageUrl: {}", bannerId, imageUrl);
        } catch (Exception e) {
            log.warn("圖片刪除失敗 (資料庫記錄已刪除) - ImageUrl: {}, Error: {}", imageUrl, e.getMessage());
        }
    }

    /**
     * 啟用輪播圖
     *
     * @param bannerId 輪播圖 ID
     * @return 啟用後的輪播圖實體
     * @throws BannerNotFoundException 當輪播圖不存在時
     */
    @Transactional
    public BannerEntity activateBanner(Long bannerId) {
        log.info("啟用輪播圖 - BannerId: {}", bannerId);

        BannerEntity banner = getBannerById(bannerId);
        banner.activate();

        BannerEntity activatedBanner = bannerRepository.save(banner);
        log.info("輪播圖啟用成功 - BannerId: {}", bannerId);

        return activatedBanner;
    }

    /**
     * 停用輪播圖
     *
     * @param bannerId 輪播圖 ID
     * @return 停用後的輪播圖實體
     * @throws BannerNotFoundException 當輪播圖不存在時
     */
    @Transactional
    public BannerEntity deactivateBanner(Long bannerId) {
        log.info("停用輪播圖 - BannerId: {}", bannerId);

        BannerEntity banner = getBannerById(bannerId);
        banner.deactivate();

        BannerEntity deactivatedBanner = bannerRepository.save(banner);
        log.info("輪播圖停用成功 - BannerId: {}", bannerId);

        return deactivatedBanner;
    }

    /**
     * 更新輪播圖顯示順序
     *
     * @param bannerId  輪播圖 ID
     * @param newOrder 新的顯示順序
     * @return 更新後的輪播圖實體
     * @throws BannerNotFoundException      當輪播圖不存在時
     * @throws InvalidBannerOrderException 當順序無效時
     */
    @Transactional
    public BannerEntity updateBannerOrder(Long bannerId, Integer newOrder) {
        log.info("更新輪播圖順序 - BannerId: {}, NewOrder: {}", bannerId, newOrder);

        // 驗證顯示順序
        validateDisplayOrder(newOrder);

        BannerEntity banner = getBannerById(bannerId);
        banner.updateOrder(newOrder);

        BannerEntity updatedBanner = bannerRepository.save(banner);
        log.info("輪播圖順序更新成功 - BannerId: {}, NewOrder: {}", bannerId, newOrder);

        return updatedBanner;
    }

    /**
     * 驗證顯示順序
     *
     * @param displayOrder 顯示順序
     * @throws InvalidBannerOrderException 當順序無效時
     */
    private void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder < 0) {
            throw new InvalidBannerOrderException(displayOrder);
        }
    }

    /**
     * 驗證上架/下架時間
     *
     * @param publishedAt   上架時間
     * @param unpublishedAt 下架時間
     * @throws IllegalArgumentException 當時間設定無效時
     */
    private void validatePublishSchedule(LocalDateTime publishedAt, LocalDateTime unpublishedAt) {
        // 若兩個時間都有設定，檢查下架時間是否晚於上架時間
        if (publishedAt != null && unpublishedAt != null) {
            if (unpublishedAt.isBefore(publishedAt) || unpublishedAt.isEqual(publishedAt)) {
                throw new IllegalArgumentException(
                    String.format("下架時間 (%s) 必須晚於上架時間 (%s)", unpublishedAt, publishedAt)
                );
            }
        }
    }
}
