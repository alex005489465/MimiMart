package com.mimimart.shared.validation;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 檔案驗證工具
 * 用於驗證上傳檔案的格式、大小等
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
public class FileValidator {

    // 允許的圖片 MIME 類型
    private static final List<String> ALLOWED_IMAGE_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    // 允許的圖片副檔名
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif"
    );

    // 最大檔案大小: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 驗證圖片檔案
     *
     * @param file 上傳的檔案
     * @throws IllegalArgumentException 當檔案驗證失敗時
     */
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("檔案不能為空");
        }

        // 驗證檔案大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("檔案大小超過限制 (最大 10MB)");
        }

        // 驗證 MIME 類型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支援的檔案格式,僅允許 JPG、PNG、GIF 格式");
        }

        // 驗證副檔名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("檔案名稱不能為空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支援的檔案副檔名,僅允許 .jpg、.jpeg、.png、.gif");
        }
    }

    /**
     * 取得檔案副檔名
     *
     * @param filename 檔案名稱
     * @return 副檔名(包含點)
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 取得安全的檔案名稱(移除特殊字元)
     *
     * @param filename 原始檔案名稱
     * @return 安全的檔案名稱
     */
    public static String getSafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "avatar";
        }

        // 移除路徑分隔符和特殊字元,只保留字母、數字、點、底線、連字號
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
