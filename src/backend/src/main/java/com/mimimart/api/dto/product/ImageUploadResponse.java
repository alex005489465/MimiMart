package com.mimimart.api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 圖片上傳回應 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {

    /**
     * 圖片 URL
     */
    private String imageUrl;
}
