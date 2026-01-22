package com.sejong.userservice.client.file;

import com.sejong.userservice.client.file.dto.PreSignedUrl;
import com.sejong.userservice.support.common.util.Filepath;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3FileUploader implements FileUploader {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${spring.application.name}")
    private String serviceName;


    @Override
    public PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String fileType) {
        // 서비스별 폴더 구조: user-service/thumbnails/filename_uuid.ext
        String key = generateKey(serviceName, fileType, fileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String downloadUrl = String.format("%s/%s/%s", endpoint, bucketName, key);

        return new PreSignedUrl(
                presignedRequest.url().toString(),
                key,
                downloadUrl,
                System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()
        );
    }

    @Override
    public void delete(Filepath filePath) {
        try {
            String key = extractKeyFromUrl(filePath.path());
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("S3 파일 삭제 완료: {}", key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", filePath.path(), e);
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }

    @Override
    public Filepath getFileUrl(String key) {
        String url = String.format("%s/%s/%s", endpoint, bucketName, key);
        return Filepath.of(url);
    }

    private String generateKey(String serviceName, String dirName, String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String fileExtension = lastDotIndex != -1 ? fileName.substring(lastDotIndex) : "";
        String uuid = UUID.randomUUID().toString();
        String cleanFileName = lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;
        return String.format("%s/%s/%s_%s%s", serviceName, dirName, cleanFileName, uuid, fileExtension);
    }

    private String extractKeyFromUrl(String url) {
        // Garage URL 형식: http://host:port/bucket/key
        String bucketPrefix = "/" + bucketName + "/";
        int bucketIndex = url.indexOf(bucketPrefix);
        if (bucketIndex == -1) {
            throw new IllegalArgumentException("Invalid S3 URL format: " + url);
        }
        return url.substring(bucketIndex + bucketPrefix.length());
    }
}