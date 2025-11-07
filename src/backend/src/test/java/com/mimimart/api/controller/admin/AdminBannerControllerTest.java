package com.mimimart.api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.api.dto.banner.BannerIdRequest;
import com.mimimart.api.dto.banner.UpdateBannerRequest;
import com.mimimart.api.dto.banner.UpdateOrderRequest;
import com.mimimart.application.service.BannerService;
import com.mimimart.infrastructure.persistence.entity.BannerEntity;
import com.mimimart.infrastructure.persistence.entity.BannerStatus;
import com.mimimart.infrastructure.persistence.repository.BannerRepository;
import com.mimimart.infrastructure.security.CustomUserDetails;
import com.mimimart.infrastructure.storage.S3StorageService;
import com.mimimart.shared.valueobject.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminBannerController 測試類別
 * 測試後台輪播圖管理 API 端點
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("後台輪播圖 API 測試")
class AdminBannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private S3StorageService s3StorageService;

    private CustomUserDetails adminUserDetails;

    // 記錄測試中上傳的輪播圖 URL,用於清理
    private final List<String> uploadedBannerUrls = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 在每個測試開始前清理所有輪播圖資料
        bannerRepository.deleteAll();

        // 建立 ADMIN UserDetails 用於認證
        adminUserDetails = new CustomUserDetails(
                1L,
                "admin@test.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")),
                UserType.ADMIN
        );
    }

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
    @DisplayName("GET /api/admin/banner/list - 成功查詢所有輪播圖")
    void testGetAllBanners_Success() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner1 = createTestBanner("測試輪播圖1", "https://s3.test/banner1.jpg", 1, BannerStatus.ACTIVE);
        BannerEntity banner2 = createTestBanner("測試輪播圖2", "https://s3.test/banner2.jpg", 2, BannerStatus.INACTIVE);

        // When & Then
        mockMvc.perform(get("/api/admin/banner/list")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查詢成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("測試輪播圖1"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[1].title").value("測試輪播圖2"))
                .andExpect(jsonPath("$.data[1].status").value("INACTIVE"));
    }

    @Test
    @DisplayName("GET /api/admin/banner/detail - 成功查詢輪播圖詳情")
    void testGetBannerDetail_Success() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);
        banner.setLinkUrl("https://example.com");
        bannerRepository.save(banner);

        // When & Then
        mockMvc.perform(get("/api/admin/banner/detail")
                        .param("bannerId", banner.getId().toString())
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查詢成功"))
                .andExpect(jsonPath("$.data.id").value(banner.getId()))
                .andExpect(jsonPath("$.data.title").value("測試輪播圖"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://s3.test/banner.jpg"))
                .andExpect(jsonPath("$.data.linkUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data.displayOrder").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/admin/banner/detail - 輪播圖不存在時返回錯誤")
    void testGetBannerDetail_NotFound() throws Exception {
        // Given: 不存在的輪播圖 ID
        Long nonExistentId = 999999L;

        // When & Then
        mockMvc.perform(get("/api/admin/banner/detail")
                        .param("bannerId", nonExistentId.toString())
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/admin/banner/create - 成功建立輪播圖")
    void testCreateBanner_Success() throws Exception {
        // Given: 準備測試資料
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "banner.jpg",
                "image/jpeg",
                "test image content for banner create".getBytes()
        );

        // When: 建立輪播圖
        String responseJson = mockMvc.perform(multipart("/api/admin/banner/create")
                        .file(imageFile)
                        .param("title", "新輪播圖")
                        .param("linkUrl", "https://example.com")
                        .param("displayOrder", "1")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("建立成功"))
                .andExpect(jsonPath("$.data.title").value("新輪播圖"))
                .andExpect(jsonPath("$.data.imageUrl").exists())
                .andExpect(jsonPath("$.data.linkUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data.displayOrder").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then: 記錄上傳的圖片 URL 以便清理
        String imageUrl = objectMapper.readTree(responseJson).get("data").get("imageUrl").asText();
        uploadedBannerUrls.add(imageUrl);
    }

    @Test
    @DisplayName("POST /api/admin/banner/create - 圖片檔案為空時返回錯誤")
    void testCreateBanner_EmptyFile() throws Exception {
        // Given: 空的圖片檔案
        MockMultipartFile emptyFile = new MockMultipartFile(
                "imageFile",
                "banner.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/banner/create")
                        .file(emptyFile)
                        .param("title", "新輪播圖")
                        .param("displayOrder", "1")
                        .with(user(adminUserDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("圖片檔案不能為空"));
    }

    @Test
    @DisplayName("POST /api/admin/banner/update - 成功更新輪播圖資訊")
    void testUpdateBanner_Success() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("原標題", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // 準備更新請求
        UpdateBannerRequest request = new UpdateBannerRequest(
                banner.getId(),
                "新標題",
                "https://new-example.com",
                5,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/admin/banner/update")
                        .with(user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.id").value(banner.getId()))
                .andExpect(jsonPath("$.data.title").value("新標題"))
                .andExpect(jsonPath("$.data.linkUrl").value("https://new-example.com"))
                .andExpect(jsonPath("$.data.displayOrder").value(5))
                .andExpect(jsonPath("$.data.imageUrl").value("https://s3.test/banner.jpg")); // 圖片 URL 不變
    }

    @Test
    @DisplayName("POST /api/admin/banner/update-with-image - 成功更新輪播圖並替換圖片")
    void testUpdateBannerWithImage_Success() throws Exception {
        // Given: 先建立一個輪播圖並記錄其 URL
        MockMultipartFile initialFile = new MockMultipartFile(
                "imageFile",
                "initial-banner.jpg",
                "image/jpeg",
                "initial image content".getBytes()
        );
        String createResponseJson = mockMvc.perform(multipart("/api/admin/banner/create")
                        .file(initialFile)
                        .param("title", "原標題")
                        .param("displayOrder", "1")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bannerId = objectMapper.readTree(createResponseJson).get("data").get("id").asLong();
        String oldImageUrl = objectMapper.readTree(createResponseJson).get("data").get("imageUrl").asText();
        uploadedBannerUrls.add(oldImageUrl); // 記錄舊圖片 URL

        // 準備新圖片
        MockMultipartFile newImageFile = new MockMultipartFile(
                "imageFile",
                "new-banner.jpg",
                "image/jpeg",
                "new image content for update".getBytes()
        );

        // When: 更新輪播圖並替換圖片
        String updateResponseJson = mockMvc.perform(multipart("/api/admin/banner/update-with-image")
                        .file(newImageFile)
                        .param("bannerId", bannerId.toString())
                        .param("title", "新標題")
                        .param("linkUrl", "https://new-example.com")
                        .param("displayOrder", "3")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.id").value(bannerId))
                .andExpect(jsonPath("$.data.title").value("新標題"))
                .andExpect(jsonPath("$.data.imageUrl").exists())
                .andExpect(jsonPath("$.data.displayOrder").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then: 記錄新圖片 URL 以便清理
        String newImageUrl = objectMapper.readTree(updateResponseJson).get("data").get("imageUrl").asText();
        uploadedBannerUrls.add(newImageUrl);
    }

    @Test
    @DisplayName("POST /api/admin/banner/update-with-image - 圖片檔案為空時返回錯誤")
    void testUpdateBannerWithImage_EmptyFile() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("原標題", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // 空的圖片檔案
        MockMultipartFile emptyFile = new MockMultipartFile(
                "imageFile",
                "banner.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/banner/update-with-image")
                        .file(emptyFile)
                        .param("bannerId", banner.getId().toString())
                        .with(user(adminUserDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("圖片檔案不能為空"));
    }

    @Test
    @DisplayName("POST /api/admin/banner/delete - 成功刪除輪播圖")
    void testDeleteBanner_Success() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // 準備刪除請求
        BannerIdRequest request = new BannerIdRequest(banner.getId());

        // When & Then
        mockMvc.perform(post("/api/admin/banner/delete")
                        .with(user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("刪除成功"));
    }

    @Test
    @DisplayName("POST /api/admin/banner/activate - 成功啟用輪播圖")
    void testActivateBanner_Success() throws Exception {
        // Given: 建立停用的輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.INACTIVE);

        // 準備請求
        BannerIdRequest request = new BannerIdRequest(banner.getId());

        // When & Then
        mockMvc.perform(post("/api/admin/banner/activate")
                        .with(user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("啟用成功"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/admin/banner/deactivate - 成功停用輪播圖")
    void testDeactivateBanner_Success() throws Exception {
        // Given: 建立啟用的輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // 準備請求
        BannerIdRequest request = new BannerIdRequest(banner.getId());

        // When & Then
        mockMvc.perform(post("/api/admin/banner/deactivate")
                        .with(user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("停用成功"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("POST /api/admin/banner/update-order - 成功更新輪播圖順序")
    void testUpdateBannerOrder_Success() throws Exception {
        // Given: 建立測試輪播圖
        BannerEntity banner = createTestBanner("測試輪播圖", "https://s3.test/banner.jpg", 1, BannerStatus.ACTIVE);

        // 準備請求
        UpdateOrderRequest request = new UpdateOrderRequest(banner.getId(), 10);

        // When & Then
        mockMvc.perform(post("/api/admin/banner/update-order")
                        .with(user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("順序更新成功"))
                .andExpect(jsonPath("$.data.displayOrder").value(10));
    }

    @Test
    @DisplayName("權限驗證 - 未認證時應拒絕存取")
    void testAccessDenied_Unauthenticated() throws Exception {
        // When & Then: 未提供認證資訊時應被拒絕 (返回 403 Forbidden)
        mockMvc.perform(get("/api/admin/banner/list"))
                .andExpect(status().isForbidden());
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
