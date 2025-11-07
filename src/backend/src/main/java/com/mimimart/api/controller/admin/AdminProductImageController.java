package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.product.ImageUploadResponse;
import com.mimimart.infrastructure.storage.S3StorageService;
import com.mimimart.shared.validation.FileValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 後台商品圖片管理 Controller
 */
@RestController
@RequestMapping("/api/admin/product/image")
@RequiredArgsConstructor
@Tag(name = "後台 - 商品圖片管理", description = "後台商品圖片上傳、刪除 API")
public class AdminProductImageController {

    private final S3StorageService s3StorageService;

    /**
     * 上傳商品圖片
     */
    @PostMapping("/upload")
    @Operation(summary = "上傳商品圖片", description = "上傳商品圖片到 S3,返回完整 URL")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("image") MultipartFile file) {

        // 驗證檔案
        FileValidator.validateImageFile(file);

        // 上傳到 S3
        String imageUrl = s3StorageService.uploadProductImage(file);

        ImageUploadResponse response = new ImageUploadResponse(imageUrl);
        return ResponseEntity.ok(ApiResponse.success("圖片上傳成功", response));
    }

    /**
     * 刪除商品圖片
     */
    @PostMapping("/delete")
    @Operation(summary = "刪除商品圖片", description = "從 S3 刪除商品圖片")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @RequestParam String imageUrl) {

        s3StorageService.deleteProductImage(imageUrl);
        return ResponseEntity.ok(ApiResponse.success("圖片刪除成功"));
    }
}
