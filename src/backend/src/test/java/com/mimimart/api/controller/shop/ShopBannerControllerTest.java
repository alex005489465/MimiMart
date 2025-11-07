package com.mimimart.api.controller.shop;

import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import com.mimimart.infrastructure.persistence.repository.BannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ShopBannerController 測試類別
 * 測試前台輪播圖 API 端點
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("前台輪播圖 API 測試")
class ShopBannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BannerRepository bannerRepository;

    @BeforeEach
    void setUp() {
        // 在每個測試開始前清理所有輪播圖資料
        bannerRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 成功查詢啟用的輪播圖")
    void testGetActiveBanners_Success() throws Exception {
        // Given: 建立測試輪播圖(2 個啟用、1 個停用)
        BannerEntity banner1 = createTestBanner("啟用輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity banner2 = createTestBanner("啟用輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.ACTIVE);
        BannerEntity banner3 = createTestBanner("停用輪播圖", "https://s3.test/banner3.jpg", 3, BannerStatus.INACTIVE);

        // When & Then: 應只返回啟用的輪播圖
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查詢成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("啟用輪播圖1"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[1].title").value("啟用輪播圖2"))
                .andExpect(jsonPath("$.data[1].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 按 displayOrder 排序返回")
    void testGetActiveBanners_OrderByDisplayOrder() throws Exception {
        // Given: 建立測試輪播圖(順序為 3, 1, 2)
        BannerEntity banner1 = createTestBanner("輪播圖3", "https://s3.test/banner3.jpg", 3, BannerStatus.ACTIVE);
        BannerEntity banner2 = createTestBanner("輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity banner3 = createTestBanner("輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.ACTIVE);

        // When & Then: 驗證按 displayOrder 升序排序
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].title").value("輪播圖1"))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1))
                .andExpect(jsonPath("$.data[1].title").value("輪播圖2"))
                .andExpect(jsonPath("$.data[1].displayOrder").value(2))
                .andExpect(jsonPath("$.data[2].title").value("輪播圖3"))
                .andExpect(jsonPath("$.data[2].displayOrder").value(3));
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 沒有啟用的輪播圖時返回空陣列")
    void testGetActiveBanners_EmptyResult() throws Exception {
        // Given: 只建立停用的輪播圖
        BannerEntity banner1 = createTestBanner("停用輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.INACTIVE);
        BannerEntity banner2 = createTestBanner("停用輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.INACTIVE);

        // When & Then: 應返回空陣列
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查詢成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 驗證返回完整的輪播圖資訊")
    void testGetActiveBanners_CompleteData() throws Exception {
        // Given: 建立測試輪播圖(含連結)
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);
        banner.setLinkUrl("https://example.com");
        bannerRepository.save(banner);

        // When & Then: 驗證返回完整資訊
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(banner.getId()))
                .andExpect(jsonPath("$.data[0].title").value("測試輪播圖"))
                .andExpect(jsonPath("$.data[0].imageUrl").value("https://s3.test/banner.jpg"))
                .andExpect(jsonPath("$.data[0].linkUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 公開端點不需認證即可存取")
    void testGetActiveBanners_PublicAccess() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("公開輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // When & Then: 不提供認證資訊也能存取
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 驗證只返回 ACTIVE 狀態的輪播圖")
    void testGetActiveBanners_OnlyActiveStatus() throws Exception {
        // Given: 建立各種狀態的輪播圖
        BannerEntity activeBanner1 = createTestBanner("啟用1", "https://s3.test/active1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity activeBanner2 = createTestBanner("啟用2", "https://s3.test/active2.jpg", 2, BannerStatus.ACTIVE);
        BannerEntity inactiveBanner1 = createTestBanner("停用1", "https://s3.test/inactive1.jpg", 3, BannerStatus.INACTIVE);
        BannerEntity inactiveBanner2 = createTestBanner("停用2", "https://s3.test/inactive2.jpg", 4, BannerStatus.INACTIVE);
        BannerEntity activeBanner3 = createTestBanner("啟用3", "https://s3.test/active3.jpg", 5, BannerStatus.ACTIVE);

        // When & Then: 驗證只返回 3 個 ACTIVE 的輪播圖
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].title").value("啟用1"))
                .andExpect(jsonPath("$.data[1].title").value("啟用2"))
                .andExpect(jsonPath("$.data[2].title").value("啟用3"));
    }

    @Test
    @DisplayName("GET /api/shop/banner/list - 輪播圖無連結時 linkUrl 為 null")
    void testGetActiveBanners_NullLinkUrl() throws Exception {
        // Given: 建立無連結的輪播圖
        BannerEntity banner = createTestBanner("無連結輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);
        // linkUrl 預設為 null

        // When & Then: 驗證 linkUrl 為 null
        mockMvc.perform(get("/api/shop/banner/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].linkUrl").isEmpty());
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
        // 設定為立即上架、永不下架
        banner.setPublishedAt(null);
        banner.setUnpublishedAt(null);

        return bannerRepository.save(banner);
    }
}
