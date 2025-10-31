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

    public S3StorageService(
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.credentials.access-key-id}") String accessKeyId,
            @Value("${aws.credentials.secret-access-key}") String secretAccessKey) {

        this.bucketName = bucketName;

        // 初始化 S3 客戶端
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("S3StorageService 已初始化 - Region: {}, Bucket: {}", region, bucketName);
    }

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

            // 上傳到 S3
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
}
