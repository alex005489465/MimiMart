package com.mimimart.infrastructure.storage;

import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.persistence.entity.Member;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * S3 儲存服務整合測試
 * 使用真實的 S3 連接進行測試
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@DisplayName("S3 儲存服務整合測試")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class S3StorageServiceIntegrationTest {

    @Autowired
    private S3StorageService s3StorageService;

    @Autowired
    private TestFixtures testFixtures;

    // 測試會員
    private Member testMember;

    // 記錄上傳的檔案 key,用於清理
    private final List<String> uploadedKeys = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 建立測試會員
        testMember = testFixtures.createTestMember(1);
        System.out.println("建立測試會員 - ID: " + testMember.getId() + ", Email: " + testMember.getEmail());
    }

    @AfterEach
    void cleanup() {
        // 清理所有測試上傳的檔案
        uploadedKeys.forEach(key -> {
            try {
                s3StorageService.deleteAvatar(key);
                System.out.println("已清理測試檔案: " + key);
            } catch (Exception e) {
                System.err.println("清理測試檔案失敗: " + key + " - " + e.getMessage());
            }
        });
        uploadedKeys.clear();
    }

    @Test
    @Order(1)
    @DisplayName("測試上傳頭貼到 S3")
    void testUploadAvatar() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test-upload.jpg",
                "image/jpeg",
                "This is a test image content for upload".getBytes()
        );

        // Act
        String s3Key = s3StorageService.uploadAvatar(testMember.getId(), file);
        uploadedKeys.add(s3Key);

        // Assert
        assertThat(s3Key).isNotNull();
        assertThat(s3Key).startsWith("avatars/" + testMember.getId() + "/");
        assertThat(s3Key).contains("test-upload.jpg");
        System.out.println("✅ 上傳成功,S3 Key: " + s3Key);
    }

    @Test
    @Order(2)
    @DisplayName("測試檢查頭貼是否存在")
    void testAvatarExists() {
        // Arrange - 先上傳一個檔案
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test-exists.jpg",
                "image/jpeg",
                "Test content for exists check".getBytes()
        );
        String s3Key = s3StorageService.uploadAvatar(testMember.getId(), file);
        uploadedKeys.add(s3Key);

        // Act & Assert
        assertThat(s3StorageService.avatarExists(s3Key)).isTrue();
        assertThat(s3StorageService.avatarExists("avatars/" + testMember.getId() + "/non-existent.jpg")).isFalse();
        System.out.println("✅ 檔案存在性檢查正常");
    }

    @Test
    @Order(3)
    @DisplayName("測試從 S3 下載頭貼")
    void testDownloadAvatar() {
        // Arrange
        byte[] originalContent = "This is original avatar content for download test".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test-download.jpg",
                "image/jpeg",
                originalContent
        );
        String s3Key = s3StorageService.uploadAvatar(testMember.getId(), file);
        uploadedKeys.add(s3Key);

        // Act
        byte[] downloadedContent = s3StorageService.downloadAvatar(s3Key);

        // Assert
        assertThat(downloadedContent).isNotNull();
        assertThat(downloadedContent).isEqualTo(originalContent);
        System.out.println("✅ 下載成功,內容大小: " + downloadedContent.length + " bytes");
    }

    @Test
    @Order(4)
    @DisplayName("測試取得 Content-Type")
    void testGetContentType() {
        // Arrange - JPG
        MockMultipartFile jpgFile = new MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                "JPG content".getBytes()
        );
        String jpgKey = s3StorageService.uploadAvatar(testMember.getId(), jpgFile);
        uploadedKeys.add(jpgKey);

        // Act & Assert - JPG
        String jpgContentType = s3StorageService.getContentType(jpgKey);
        assertThat(jpgContentType).isEqualTo("image/jpeg");
        System.out.println("✅ JPG Content-Type 正確: " + jpgContentType);

        // Arrange - PNG
        MockMultipartFile pngFile = new MockMultipartFile(
                "avatar",
                "test.png",
                "image/png",
                "PNG content".getBytes()
        );
        String pngKey = s3StorageService.uploadAvatar(testMember.getId(), pngFile);
        uploadedKeys.add(pngKey);

        // Act & Assert - PNG
        String pngContentType = s3StorageService.getContentType(pngKey);
        assertThat(pngContentType).isEqualTo("image/png");
        System.out.println("✅ PNG Content-Type 正確: " + pngContentType);
    }

    @Test
    @Order(5)
    @DisplayName("測試刪除頭貼")
    void testDeleteAvatar() {
        // Arrange - 上傳檔案
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test-delete.jpg",
                "image/jpeg",
                "Content to be deleted".getBytes()
        );
        String s3Key = s3StorageService.uploadAvatar(testMember.getId(), file);

        // 確認檔案存在
        assertThat(s3StorageService.avatarExists(s3Key)).isTrue();

        // Act - 刪除檔案
        s3StorageService.deleteAvatar(s3Key);

        // Assert - 確認檔案不存在
        assertThat(s3StorageService.avatarExists(s3Key)).isFalse();
        System.out.println("✅ 刪除成功,S3 Key: " + s3Key);
    }

    @Test
    @Order(6)
    @DisplayName("測試下載不存在的檔案")
    void testDownloadNonExistentFile() {
        // Arrange
        String nonExistentKey = "avatars/" + testMember.getId() + "/non-existent-file.jpg";

        // Act & Assert
        assertThatThrownBy(() -> s3StorageService.downloadAvatar(nonExistentKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("頭貼不存在");
        System.out.println("✅ 下載不存在的檔案時正確拋出例外");
    }

    @Test
    @Order(7)
    @DisplayName("測試刪除不存在的檔案(冪等性)")
    void testDeleteNonExistentFile() {
        // Arrange
        String nonExistentKey = "avatars/" + testMember.getId() + "/already-deleted.jpg";

        // Act & Assert - 刪除不存在的檔案應該成功(冪等性)
        assertThatCode(() -> s3StorageService.deleteAvatar(nonExistentKey))
                .doesNotThrowAnyException();
        System.out.println("✅ 刪除不存在的檔案時保持冪等性");
    }

    @Test
    @Order(8)
    @DisplayName("測試完整的替換流程")
    void testReplaceAvatar() {
        // Arrange - 上傳第一個頭貼
        MockMultipartFile oldFile = new MockMultipartFile(
                "avatar",
                "old-avatar.jpg",
                "image/jpeg",
                "Old avatar content".getBytes()
        );
        String oldKey = s3StorageService.uploadAvatar(testMember.getId(), oldFile);
        uploadedKeys.add(oldKey);

        // 確認舊檔案存在
        assertThat(s3StorageService.avatarExists(oldKey)).isTrue();

        // Act - 上傳新頭貼並刪除舊的
        MockMultipartFile newFile = new MockMultipartFile(
                "avatar",
                "new-avatar.jpg",
                "image/jpeg",
                "New avatar content".getBytes()
        );
        String newKey = s3StorageService.uploadAvatar(testMember.getId(), newFile);
        uploadedKeys.add(newKey);

        // 刪除舊頭貼
        s3StorageService.deleteAvatar(oldKey);

        // Assert
        assertThat(s3StorageService.avatarExists(oldKey)).isFalse();
        assertThat(s3StorageService.avatarExists(newKey)).isTrue();

        // 驗證新頭貼內容
        byte[] downloadedNew = s3StorageService.downloadAvatar(newKey);
        assertThat(new String(downloadedNew)).isEqualTo("New avatar content");

        System.out.println("✅ 替換頭貼流程正常");
        System.out.println("   舊 Key: " + oldKey + " (已刪除)");
        System.out.println("   新 Key: " + newKey + " (存在)");
    }

    @Test
    @Order(9)
    @DisplayName("測試大檔案上傳 (接近 5MB)")
    void testLargeFileUpload() {
        // Arrange - 建立接近 5MB 的檔案 (4.5MB)
        int fileSize = 4 * 1024 * 1024 + 512 * 1024; // 4.5MB
        byte[] largeContent = new byte[fileSize];
        for (int i = 0; i < fileSize; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        MockMultipartFile largeFile = new MockMultipartFile(
                "avatar",
                "large-avatar.jpg",
                "image/jpeg",
                largeContent
        );

        // Act
        String s3Key = s3StorageService.uploadAvatar(testMember.getId(), largeFile);
        uploadedKeys.add(s3Key);

        // Assert
        assertThat(s3StorageService.avatarExists(s3Key)).isTrue();

        // 驗證下載的內容完整
        byte[] downloaded = s3StorageService.downloadAvatar(s3Key);
        assertThat(downloaded.length).isEqualTo(fileSize);
        assertThat(downloaded).isEqualTo(largeContent);

        System.out.println("✅ 大檔案上傳成功,大小: " + (fileSize / 1024.0 / 1024.0) + " MB");
    }
}
