package com.mimimart.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * S3 儲存服務
 * 處理檔案上傳、下載、刪除等操作
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicBucketName;
    private final String publicBaseUrl;

    public S3StorageService(
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.public-bucket-name}") String publicBucketName,
            @Value("${aws.s3.public-base-url}") String publicBaseUrl,
            @Value("${aws.credentials.access-key-id}") String accessKeyId,
            @Value("${aws.credentials.secret-access-key}") String secretAccessKey) {

        this.bucketName = bucketName;
        this.publicBucketName = publicBucketName;
        this.publicBaseUrl = publicBaseUrl;

        // 初始化 S3 客戶端
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("S3StorageService 已初始化 - Region: {}, Bucket: {}, Public Bucket: {}, Public Base URL: {}",
                region, bucketName, publicBucketName, publicBaseUrl);
    }

    // ===== 會員頭貼相關方法 =====

    /**
     * 上傳會員頭貼到 S3
     *
     * @param memberId 會員 ID
     * @param file     上傳的檔案
     * @return S3 物件的 key
     * @throws RuntimeException 當上傳失敗時
     */
    public String uploadAvatar(Long memberId, MultipartFile file) {
        try {
            // 生成 S3 key: avatars/{memberId}/{timestamp}_{filename}
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String safeFilename = file.getOriginalFilename();
            String s3Key = String.format("avatars/%d/%s_%s", memberId, timestamp, safeFilename);

            // 上傳到 S3 私有 bucket
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("頭貼上傳成功 - MemberId: {}, S3 Key: {}", memberId, s3Key);
            return s3Key;

        } catch (S3Exception e) {
            log.error("S3 上傳失敗 - MemberId: {}, Error: {}", memberId, e.getMessage(), e);
            throw new RuntimeException("頭貼上傳失敗: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("讀取檔案失敗 - MemberId: {}, Error: {}", memberId, e.getMessage(), e);
            throw new RuntimeException("讀取檔案失敗", e);
        }
    }

    /**
     * 從 S3 下載頭貼
     *
     * @param s3Key S3 物件的 key
     * @return 檔案內容的位元組陣列
     * @throws RuntimeException 當下載失敗時
     */
    public byte[] downloadAvatar(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // 讀取 S3 物件內容
            try (InputStream inputStream = s3Client.getObject(getObjectRequest);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                log.info("頭貼下載成功 - S3 Key: {}", s3Key);
                return outputStream.toByteArray();
            }

        } catch (NoSuchKeyException e) {
            log.error("S3 物件不存在 - S3 Key: {}", s3Key);
            throw new RuntimeException("頭貼不存在", e);
        } catch (S3Exception e) {
            log.error("S3 下載失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
            throw new RuntimeException("頭貼下載失敗: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("讀取 S3 串流失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
            throw new RuntimeException("讀取頭貼失敗", e);
        }
    }

    /**
     * 從 S3 刪除頭貼
     *
     * @param s3Key S3 物件的 key
     */
    public void deleteAvatar(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("頭貼刪除成功 - S3 Key: {}", s3Key);

        } catch (S3Exception e) {
            // 如果物件不存在,也視為刪除成功(冪等性)
            if (e.statusCode() == 404) {
                log.warn("S3 物件不存在,視為刪除成功 - S3 Key: {}", s3Key);
            } else {
                log.error("S3 刪除失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
                throw new RuntimeException("頭貼刪除失敗: " + e.awsErrorDetails().errorMessage(), e);
            }
        }
    }

    /**
     * 檢查 S3 物件是否存在
     *
     * @param s3Key S3 物件的 key
     * @return 是否存在
     */
    public boolean avatarExists(String s3Key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("檢查 S3 物件存在性失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 取得 S3 物件的 Content-Type
     *
     * @param s3Key S3 物件的 key
     * @return Content-Type
     */
    public String getContentType(String s3Key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentType();

        } catch (S3Exception e) {
            log.error("取得 Content-Type 失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
            return "application/octet-stream"; // 預設值
        }
    }

    // ===== AI 生成圖片相關方法 =====
    // TODO: AI 生成圖片(ai-generated/)自動清理尚未實作
    //   方案1: S3 Lifecycle Policy 設定 30 天自動刪除 (推薦)
    //   方案2: Spring Boot Scheduled Task 定期清理過期檔案

    /**
     * 上傳 AI 生成的圖片到 S3 私有 bucket
     *
     * @param imageData    圖片資料
     * @param contentType  Content-Type (例: image/png)
     * @param fileExtension 檔案副檔名 (例: .png)
     * @return S3 物件的 key
     * @throws RuntimeException 當上傳失敗時
     */
    public String uploadAiImage(byte[] imageData, String contentType, String fileExtension) {
        try {
            // 生成 S3 key: ai-generated/ai-{timestamp}{extension}
            String timestamp = String.valueOf(System.currentTimeMillis());
            String s3Key = String.format("ai-generated/ai-%s%s", timestamp, fileExtension);

            // 上傳到 S3 私有 bucket
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) imageData.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageData));

            log.info("AI 圖片上傳成功 - S3 Key: {}, Size: {} bytes", s3Key, imageData.length);
            return s3Key;

        } catch (S3Exception e) {
            log.error("AI 圖片上傳失敗 - Error: {}", e.getMessage(), e);
            throw new RuntimeException("AI 圖片上傳失敗: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * 從 S3 下載 AI 生成的圖片
     *
     * @param s3Key S3 物件的 key
     * @return 檔案內容的位元組陣列
     * @throws RuntimeException 當下載失敗時
     */
    public byte[] downloadAiImage(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // 讀取 S3 物件內容
            try (InputStream inputStream = s3Client.getObject(getObjectRequest);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                log.info("AI 圖片下載成功 - S3 Key: {}", s3Key);
                return outputStream.toByteArray();
            }

        } catch (NoSuchKeyException e) {
            log.error("AI 圖片不存在 - S3 Key: {}", s3Key);
            throw new RuntimeException("AI 圖片不存在", e);
        } catch (S3Exception e) {
            log.error("AI 圖片下載失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
            throw new RuntimeException("AI 圖片下載失敗: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("讀取 AI 圖片失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
            throw new RuntimeException("讀取 AI 圖片失敗", e);
        }
    }

    /**
     * 刪除 AI 生成的圖片
     *
     * @param s3Key S3 物件的 key
     */
    public void deleteAiImage(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("AI 圖片刪除成功 - S3 Key: {}", s3Key);

        } catch (S3Exception e) {
            // 如果物件不存在,也視為刪除成功(冪等性)
            if (e.statusCode() == 404) {
                log.warn("AI 圖片不存在,視為刪除成功 - S3 Key: {}", s3Key);
            } else {
                log.error("AI 圖片刪除失敗 - S3 Key: {}, Error: {}", s3Key, e.getMessage(), e);
                throw new RuntimeException("AI 圖片刪除失敗: " + e.awsErrorDetails().errorMessage(), e);
            }
        }
    }

    // ===== 輪播圖相關方法 =====

    /**
     * 上傳輪播圖到 S3 公開 Bucket
     *
     * @param file 上傳的檔案
     * @return S3 物件的 URL (完整路徑)
     * @throws RuntimeException 當上傳失敗時
     */
    public String uploadBanner(MultipartFile file) {
        try {
            // 生成 S3 key: banners/banner-{timestamp}.{extension}
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String s3Key = String.format("banners/banner-%s%s", timestamp, extension);

            // 上傳到公開 Bucket
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(publicBucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 生成公開 URL (使用自訂域名)
            String publicUrl = String.format("%s/%s", publicBaseUrl, s3Key);
            log.info("輪播圖上傳成功 - S3 Key: {}, URL: {}", s3Key, publicUrl);
            return publicUrl;

        } catch (S3Exception e) {
            log.error("S3 上傳輪播圖失敗 - Error: {}", e.getMessage(), e);
            throw new RuntimeException("輪播圖上傳失敗: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("讀取輪播圖檔案失敗 - Error: {}", e.getMessage(), e);
            throw new RuntimeException("讀取檔案失敗", e);
        }
    }

    /**
     * 上傳商品圖片到 S3 公開 Bucket
     *
     * @param file 上傳的檔案
     * @return S3 物件的 URL (完整路徑)
     * @throws RuntimeException 當上傳失敗時
     */
    public String uploadProductImage(MultipartFile file) {
        try {
            // 生成 S3 key: products/product-{timestamp}.{extension}
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String s3Key = String.format("products/product-%s%s", timestamp, extension);

            // 上傳到公開 Bucket
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(publicBucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 生成公開 URL (使用自訂域名)
            String publicUrl = String.format("%s/%s", publicBaseUrl, s3Key);
            log.info("商品圖片上傳成功 - S3 Key: {}, URL: {}", s3Key, publicUrl);
            return publicUrl;

        } catch (S3Exception e) {
            log.error("S3 上傳商品圖片失敗 - Error: {}", e.getMessage(), e);
            throw new RuntimeException("商品圖片上傳失敗: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("讀取商品圖片檔案失敗 - Error: {}", e.getMessage(), e);
            throw new RuntimeException("讀取檔案失敗", e);
        }
    }

    /**
     * 從 S3 刪除商品圖片
     *
     * @param imageUrl 圖片的完整 URL
     */
    public void deleteProductImage(String imageUrl) {
        try {
            // 從完整 URL 中提取 S3 key
            String s3Key = imageUrl.replace(publicBaseUrl + "/", "");

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(publicBucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("商品圖片刪除成功 - S3 Key: {}", s3Key);

        } catch (S3Exception e) {
            // 如果物件不存在,也視為刪除成功(冪等性)
            if (e.statusCode() == 404) {
                log.warn("S3 物件不存在,視為刪除成功 - URL: {}", imageUrl);
            } else {
                log.error("S3 刪除商品圖片失敗 - URL: {}, Error: {}", imageUrl, e.getMessage(), e);
                throw new RuntimeException("商品圖片刪除失敗: " + e.awsErrorDetails().errorMessage(), e);
            }
        }
    }

    /**
     * 從 S3 刪除輪播圖
     *
     * @param imageUrl 圖片的完整 URL
     */
    public void deleteBanner(String imageUrl) {
        try {
            // 從 URL 提取 S3 Key
            // 範例: https://mimimart-public.s3.amazonaws.com/banners/banner-1234567890.jpg
            //       -> banners/banner-1234567890.jpg
            String s3Key = extractS3KeyFromUrl(imageUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(publicBucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("輪播圖刪除成功 - S3 Key: {}", s3Key);

        } catch (S3Exception e) {
            // 如果物件不存在,也視為刪除成功(冪等性)
            if (e.statusCode() == 404) {
                log.warn("S3 輪播圖不存在,視為刪除成功 - URL: {}", imageUrl);
            } else {
                log.error("S3 刪除輪播圖失敗 - URL: {}, Error: {}", imageUrl, e.getMessage(), e);
                throw new RuntimeException("輪播圖刪除失敗: " + e.awsErrorDetails().errorMessage(), e);
            }
        }
    }

    /**
     * 從 S3 URL 提取 S3 Key
     *
     * @param url S3 物件的完整 URL
     * @return S3 Key
     */
    private String extractS3KeyFromUrl(String url) {
        // 優先支援自訂域名格式
        // 例: http://shop-storage-public-dev.xenolume.com/banners/banner-123.jpg
        //     -> banners/banner-123.jpg
        if (url.startsWith(publicBaseUrl)) {
            String key = url.substring(publicBaseUrl.length());
            // 移除開頭的 /
            return key.startsWith("/") ? key.substring(1) : key;
        }

        // 向下相容: 支援 AWS S3 標準 URL 格式
        // 格式 1: https://bucket-name.s3.amazonaws.com/key
        if (url.contains(".s3.amazonaws.com/")) {
            return url.substring(url.indexOf(".s3.amazonaws.com/") + 18);
        }

        // 格式 2: https://s3.amazonaws.com/bucket-name/key
        if (url.contains("s3.amazonaws.com/")) {
            String afterS3 = url.substring(url.indexOf("s3.amazonaws.com/") + 17);
            // 跳過 bucket name
            return afterS3.substring(afterS3.indexOf("/") + 1);
        }

        // 假設傳入的已經是 S3 Key
        log.warn("無法從 URL 解析 S3 Key,假設傳入的是 Key 本身: {}", url);
        return url;
    }
}
