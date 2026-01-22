package com.sejong.userservice.client.file;

import com.sejong.userservice.client.file.dto.PreSignedUrl;
import com.sejong.userservice.support.common.util.Filepath;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
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

    // Presigned URL용 key 생성 (temp 포함)
    private String generateKey(String serviceName, String dirName, String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String fileExtension = lastDotIndex != -1 ? fileName.substring(lastDotIndex) : "";
        String uuid = UUID.randomUUID().toString();
        String cleanFileName = lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;

        // temp 폴더에 먼저 업로드
        return String.format("temp/%s/%s/%s_%s%s", serviceName, dirName, cleanFileName, uuid, fileExtension);
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

    @Override
    public Filepath moveFile(String sourceKey, String targetDirectory) {
        // sourceKey: temp/user-service/profiles/image_uuid.jpg
        // targetDirectory: user-service/users/{userId}/profiles

        String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
        String targetKey = String.format("%s/%s", targetDirectory, fileName);

        try {
            // 1. 복사
            s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(targetKey)
                .build());

            // 2. 원본 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(sourceKey)
                .build());

            log.info("S3 파일 이동 완료: {} -> {}", sourceKey, targetKey);
            return getFileUrl(targetKey);

        } catch (Exception e) {
            log.error("S3 파일 이동 실패: {} -> {}", sourceKey, targetDirectory, e);
            throw new RuntimeException("파일 이동 실패", e);
        }
    }

    // 파일 직접 업로드 (프로필 사진, 썸네일 등)
    @Override
    public Filepath uploadFile(MultipartFile file, String directory, String fileName) {
        try {
            // key 생성: user-service/users/123/profile/image_uuid.jpg
            String key = generateDirectUploadKey(directory, fileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            // S3에 직접 업로드
            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.info("S3 파일 업로드 완료: {}", key);
            return getFileUrl(key);

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", fileName, e);
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    /**
     * 직접 업로드용 key 생성 (temp 없음)
     */
    private String generateDirectUploadKey(String directory, String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String fileExtension = lastDotIndex != -1 ? fileName.substring(lastDotIndex) : "";
        String uuid = UUID.randomUUID().toString();
        String cleanFileName = lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;

        // user-service/users/123/profile/image_uuid.jpg (temp 없이 바로 최종 위치)
        return String.format("%s/%s_%s%s", directory, cleanFileName, uuid, fileExtension);
    }
}