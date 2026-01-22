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

/**
 * S3 호환 스토리지 (Garage) 파일 업로더
 *
 * 지원 방식:
 * 1. Presigned URL - generatePreSignedUrl() + moveFile()
 * 2. 직접 업로드 - uploadFile()
 */
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

    // 공통
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

    // 공통
    @Override
    public Filepath getFileUrl(String key) {
        String url = String.format("%s/%s/%s", endpoint, bucketName, key);
        return Filepath.of(url);
    }

    /**
     * Presigned URL 생성
     * - temp 폴더에 업로드 후, 저장 시 moveFile()로 최종 위치 이동
     * - 용도: 에디터 이미지, 대용량 파일
     */
    @Override
    public PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String fileType) {
        String key = generateTempKey(serviceName, fileType, fileName);

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

    /**
     * Presigned URL용 key 생성 (temp 폴더 포함)
     * 결과: temp/{service}/{type}/{filename}_{uuid}.{ext}
     */
    private String generateTempKey(String serviceName, String dirName, String fileName) {
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

    /**
     * temp 파일을 최종 위치로 이동 (복사 후 원본 삭제)
     * @param sourceKey temp/project-service/projects/image_uuid.jpg
     * @param targetDirectory project-service/projects/{projectId}/images
     */
    @Override
    public Filepath moveFile(String sourceKey, String targetDirectory) {
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

    // 직접 업로드
    /**
     * 파일 직접 업로드 (temp 거치지 않고 바로 최종 위치)
     * - 용도: 프로필 이미지, 썸네일 등 소용량 파일
     * @param directory user-service/users/{userId}/profile
     * @param fileName 원본 파일명
     */
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
     * 직접 업로드용 key 생성 (temp 없이 바로 최종 위치)
     * 결과: {directory}/{filename}_{uuid}.{ext}
     */
    private String generateDirectUploadKey(String directory, String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String fileExtension = lastDotIndex != -1 ? fileName.substring(lastDotIndex) : "";
        String uuid = UUID.randomUUID().toString();
        String cleanFileName = lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;

        return String.format("%s/%s_%s%s", directory, cleanFileName, uuid, fileExtension);
    }
}