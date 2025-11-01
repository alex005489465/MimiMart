package com.mimimart.application.service;

import com.mimimart.domain.banner.exception.BannerNotFoundException;
import com.mimimart.domain.banner.exception.InvalidBannerOrderException;
import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import com.mimimart.infrastructure.persistence.repository.BannerRepository;
import com.mimimart.infrastructure.storage.S3StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BannerService 測試類別
 * 測試輪播圖相關功能
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@SpringBootTest
@Transactional
@DisplayName("輪播圖服務測試")
class BannerServiceTest {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private S3StorageService s3StorageService;

    // 記錄測試中上傳的輪播圖 URL,用於清理
    private final List<String> uploadedBannerUrls = new ArrayList<>();

    @AfterEach
    void cleanup() {
        // 清理測試中上傳的所有輪播圖
        uploadedBannerUrls.forEach(url -> {
            try {
                s3StorageService.deleteBanner(url);
                System.out.println("已清理測試輪播圖: " + url);
            } catch (Exception e) {
                System.err.println("清理測試輪播圖失敗: " + url + " - " + e.getMessage());
            }
        });
        uploadedBannerUrls.clear();
    }

    @Test
    @DisplayName("查詢啟用的輪播圖 - 成功取得啟用的輪播圖列表")
    void testGetActiveBanners_Success() {
        // Given: 建立測試輪播圖資料(2 個啟用、1 個停用)
        BannerEntity banner1 = createTestBanner("測試輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity banner2 = createTestBanner("測試輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.ACTIVE);
        BannerEntity banner3 = createTestBanner("測試輪播圖3", "https://s3.test/banner3.jpg", 3, BannerStatus.INACTIVE);

        // When: 查詢啟用的輪播圖
        List<BannerEntity> activeBanners = bannerService.getActiveBanners();

        // Then: 驗證只返回啟用的輪播圖,且按順序排序
        assertNotNull(activeBanners);
        assertEquals(2, activeBanners.size());
        assertEquals("測試輪播圖1", activeBanners.get(0).getTitle());
        assertEquals("測試輪播圖2", activeBanners.get(1).getTitle());
        assertEquals(BannerStatus.ACTIVE, activeBanners.get(0).getStatus());
        assertEquals(BannerStatus.ACTIVE, activeBanners.get(1).getStatus());
    }

    @Test
    @DisplayName("查詢所有輪播圖 - 成功取得所有輪播圖(含停用)")
    void testGetAllBanners_Success() {
        // Given: 建立測試輪播圖資料(2 個啟用、1 個停用)
        BannerEntity banner1 = createTestBanner("測試輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity banner2 = createTestBanner("測試輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.INACTIVE);
        BannerEntity banner3 = createTestBanner("測試輪播圖3", "https://s3.test/banner3.jpg", 3, BannerStatus.ACTIVE);

        // When: 查詢所有輪播圖
        List<BannerEntity> allBanners = bannerService.getAllBanners();

        // Then: 驗證返回所有輪播圖,且按順序排序
        assertNotNull(allBanners);
        assertEquals(3, allBanners.size());
        assertEquals("測試輪播圖1", allBanners.get(0).getTitle());
        assertEquals("測試輪播圖2", allBanners.get(1).getTitle());
        assertEquals("測試輪播圖3", allBanners.get(2).getTitle());
    }

    @Test
    @DisplayName("根據 ID 查詢輪播圖 - 成功取得輪播圖詳情")
    void testGetBannerById_Success() {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // When: 根據 ID 查詢
        BannerEntity foundBanner = bannerService.getBannerById(banner.getId());

        // Then: 驗證輪播圖資訊
        assertNotNull(foundBanner);
        assertEquals(banner.getId(), foundBanner.getId());
        assertEquals("測試輪播圖", foundBanner.getTitle());
        assertEquals("https://s3.test/banner.jpg", foundBanner.getImageUrl());
    }

    @Test
    @DisplayName("根據 ID 查詢輪播圖 - 輪播圖不存在時拋出異常")
    void testGetBannerById_NotFound() {
        // Given: 不存在的輪播圖 ID
        Long nonExistentId = 999999L;

        // When & Then: 驗證拋出 BannerNotFoundException
        assertThrows(BannerNotFoundException.class, () -> {
            bannerService.getBannerById(nonExistentId);
        });
    }

    @Test
    @DisplayName("建立輪播圖 - 成功建立輪播圖並上傳圖片")
    void testCreateBanner_Success() {
        // Given: 準備測試資料
        String title = "新輪播圖";
        String linkUrl = "https://example.com";
        Integer displayOrder = 1;
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "banner.jpg",
                "image/jpeg",
                "test image content for create banner".getBytes()
        );

        // When: 建立輪播圖
        BannerEntity createdBanner = bannerService.createBanner(title, imageFile, linkUrl, displayOrder);

        // Then: 驗證輪播圖建立成功
        assertNotNull(createdBanner);
        assertNotNull(createdBanner.getId());
        assertEquals(title, createdBanner.getTitle());
        assertNotNull(createdBanner.getImageUrl()); // 驗證有 URL 產生
        assertTrue(createdBanner.getImageUrl().contains("banners/")); // 驗證 URL 格式
        assertEquals(linkUrl, createdBanner.getLinkUrl());
        assertEquals(displayOrder, createdBanner.getDisplayOrder());
        assertEquals(BannerStatus.ACTIVE, createdBanner.getStatus());

        // 記錄 URL 以便清理
        uploadedBannerUrls.add(createdBanner.getImageUrl());
    }

    @Test
    @DisplayName("建立輪播圖 - 顯示順序為負數時拋出異常")
    void testCreateBanner_InvalidOrder() {
        // Given: 準備測試資料(顯示順序為負數)
        String title = "新輪播圖";
        Integer invalidDisplayOrder = -1;
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "banner.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // When & Then: 驗證拋出 InvalidBannerOrderException
        assertThrows(InvalidBannerOrderException.class, () -> {
            bannerService.createBanner(title, imageFile, null, invalidDisplayOrder);
        });

        // 註: 因為在驗證階段就拋出異常,S3 不會被呼叫
    }

    @Test
    @DisplayName("更新輪播圖資訊 - 成功更新標題和連結")
    void testUpdateBanner_Success() {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("原標題", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // When: 更新輪播圖資訊
        String newTitle = "新標題";
        String newLinkUrl = "https://new-example.com";
        Integer newDisplayOrder = 5;
        BannerEntity updatedBanner = bannerService.updateBanner(banner.getId(), newTitle, newLinkUrl, newDisplayOrder);

        // Then: 驗證更新成功
        assertNotNull(updatedBanner);
        assertEquals(banner.getId(), updatedBanner.getId());
        assertEquals(newTitle, updatedBanner.getTitle());
        assertEquals(newLinkUrl, updatedBanner.getLinkUrl());
        assertEquals(newDisplayOrder, updatedBanner.getDisplayOrder());
        assertEquals("https://s3.test/banner.jpg", updatedBanner.getImageUrl()); // 圖片 URL 不變
    }

    @Test
    @DisplayName("更新輪播圖資訊 - 部分更新(只更新標題)")
    void testUpdateBanner_PartialUpdate() {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("原標題", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);
        banner.setLinkUrl("https://original.com");
        bannerRepository.save(banner);

        // When: 只更新標題
        String newTitle = "只改標題";
        BannerEntity updatedBanner = bannerService.updateBanner(banner.getId(), newTitle, null, null);

        // Then: 驗證只有標題改變
        assertNotNull(updatedBanner);
        assertEquals(newTitle, updatedBanner.getTitle());
        assertNull(updatedBanner.getLinkUrl()); // linkUrl 設為 null
        assertEquals(1, updatedBanner.getDisplayOrder()); // displayOrder 不變
    }

    @Test
    @DisplayName("更新輪播圖並替換圖片 - 成功更新並刪除舊圖片")
    void testUpdateBannerWithImage_Success() {
        // Given: 先建立一個輪播圖(使用真實 S3)
        MockMultipartFile initialFile = new MockMultipartFile(
                "imageFile",
                "initial-banner.jpg",
                "image/jpeg",
                "initial image content for update test".getBytes()
        );
        BannerEntity banner = bannerService.createBanner("原標題", initialFile, null, 1);
        String oldImageUrl = banner.getImageUrl();
        uploadedBannerUrls.add(oldImageUrl); // 記錄舊圖片

        // 準備新圖片
        MockMultipartFile newImageFile = new MockMultipartFile(
                "imageFile",
                "new-banner.jpg",
                "image/jpeg",
                "new image content for update".getBytes()
        );

        // When: 更新輪播圖並替換圖片
        String newTitle = "新標題";
        BannerEntity updatedBanner = bannerService.updateBannerWithImage(
                banner.getId(), newTitle, newImageFile, null, 2
        );

        // Then: 驗證更新成功
        assertNotNull(updatedBanner);
        assertEquals(newTitle, updatedBanner.getTitle());
        assertNotNull(updatedBanner.getImageUrl());
        assertNotEquals(oldImageUrl, updatedBanner.getImageUrl()); // 圖片 URL 應該改變
        assertTrue(updatedBanner.getImageUrl().contains("banners/"));
        assertEquals(2, updatedBanner.getDisplayOrder());

        // 記錄新圖片 URL
        uploadedBannerUrls.add(updatedBanner.getImageUrl());
    }

    @Test
    @DisplayName("刪除輪播圖 - 成功刪除輪播圖和 S3 圖片")
    void testDeleteBanner_Success() {
        // Given: 先建立一個輪播圖(使用真實 S3)
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "banner-to-delete.jpg",
                "image/jpeg",
                "image content for delete test".getBytes()
        );
        BannerEntity banner = bannerService.createBanner("待刪除輪播圖", imageFile, null, 1);
        Long bannerId = banner.getId();
        String imageUrl = banner.getImageUrl();

        // When: 刪除輪播圖
        bannerService.deleteBanner(bannerId);

        // Then: 驗證輪播圖已刪除
        assertFalse(bannerRepository.existsById(bannerId));

        // 註: S3 圖片應該已被刪除,所以不需要加入 uploadedBannerUrls
    }

    @Test
    @DisplayName("刪除輪播圖 - 輪播圖不存在時拋出異常")
    void testDeleteBanner_NotFound() {
        // Given: 不存在的輪播圖 ID
        Long nonExistentId = 999999L;

        // When & Then: 驗證拋出 BannerNotFoundException
        assertThrows(BannerNotFoundException.class, () -> {
            bannerService.deleteBanner(nonExistentId);
        });

        // 註: 因為輪播圖不存在,S3 刪除不會被呼叫
    }

    @Test
    @DisplayName("啟用輪播圖 - 成功啟用停用的輪播圖")
    void testActivateBanner_Success() {
        // Given: 建立停用的輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.INACTIVE);

        // When: 啟用輪播圖
        BannerEntity activatedBanner = bannerService.activateBanner(banner.getId());

        // Then: 驗證狀態變為 ACTIVE
        assertNotNull(activatedBanner);
        assertEquals(BannerStatus.ACTIVE, activatedBanner.getStatus());
    }

    @Test
    @DisplayName("停用輪播圖 - 成功停用啟用的輪播圖")
    void testDeactivateBanner_Success() {
        // Given: 建立啟用的輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // When: 停用輪播圖
        BannerEntity deactivatedBanner = bannerService.deactivateBanner(banner.getId());

        // Then: 驗證狀態變為 INACTIVE
        assertNotNull(deactivatedBanner);
        assertEquals(BannerStatus.INACTIVE, deactivatedBanner.getStatus());
    }

    @Test
    @DisplayName("更新輪播圖順序 - 成功更新顯示順序")
    void testUpdateBannerOrder_Success() {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // When: 更新顯示順序
        Integer newOrder = 10;
        BannerEntity updatedBanner = bannerService.updateBannerOrder(banner.getId(), newOrder);

        // Then: 驗證順序更新成功
        assertNotNull(updatedBanner);
        assertEquals(newOrder, updatedBanner.getDisplayOrder());
    }

    @Test
    @DisplayName("更新輪播圖順序 - 順序為負數時拋出異常")
    void testUpdateBannerOrder_InvalidOrder() {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // When & Then: 驗證拋出 InvalidBannerOrderException
        assertThrows(InvalidBannerOrderException.class, () -> {
            bannerService.updateBannerOrder(banner.getId(), -5);
        });
    }

    @Test
    @DisplayName("查詢啟用的輪播圖 - 按 displayOrder 排序")
    void testGetActiveBanners_OrderByDisplayOrder() {
        // Given: 建立測試輪播圖(順序為 3, 1, 2)
        BannerEntity banner1 = createTestBanner("輪播圖3", "https://s3.test/banner3.jpg", 3, BannerStatus.ACTIVE);
        BannerEntity banner2 = createTestBanner("輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity banner3 = createTestBanner("輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.ACTIVE);

        // When: 查詢啟用的輪播圖
        List<BannerEntity> activeBanners = bannerService.getActiveBanners();

        // Then: 驗證按 displayOrder 升序排序
        assertNotNull(activeBanners);
        assertEquals(3, activeBanners.size());
        assertEquals("輪播圖1", activeBanners.get(0).getTitle());
        assertEquals(1, activeBanners.get(0).getDisplayOrder());
        assertEquals("輪播圖2", activeBanners.get(1).getTitle());
        assertEquals(2, activeBanners.get(1).getDisplayOrder());
        assertEquals("輪播圖3", activeBanners.get(2).getTitle());
        assertEquals(3, activeBanners.get(2).getDisplayOrder());
    }

    // ===== 輔助方法 =====

    /**
     * 建立測試輪播圖
     */
    private BannerEntity createTestBanner(String title, String imageUrl, Integer displayOrder, BannerStatus status) {
        BannerEntity banner = new BannerEntity();
        banner.setTitle(title);
        banner.setImageUrl(imageUrl);
        banner.setLinkUrl(null);
        banner.setDisplayOrder(displayOrder);
        banner.setStatus(status);

        return bannerRepository.save(banner);
    }
}
