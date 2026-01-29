package com.sejong.userservice.client.file;

import com.sejong.userservice.client.file.dto.PreSignedUrl;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 인터페이스
 *
 * 두 가지 업로드 방식 지원:
 * 1. Presigned URL 방식 - 대용량 파일, 에디터 이미지 등 (프론트에서 직접 S3 업로드)
 * 2. 직접 업로드 방식 - 프로필 이미지 등 소용량 파일 (백엔드 경유)
 *
 * key/URL 처리:
 * - 내부 파일: key 저장 (user-service/users/123/profile/image.jpg)
 * - 외부 파일: 전체 URL 저장 (https://avatars.githubusercontent.com/...)
 * - getFileUrl(): 내부 key → URL 조립, 외부 URL → 그대로 반환
 */
public interface FileUploader {

    // ==================== 공통 ====================

    /**
     * 파일 삭제 (내부 key만 삭제, 외부 URL은 스킵)
     */
    void delete(String key);

    /**
     * key 또는 URL을 전체 URL로 변환
     * - 내부 key: endpoint/bucket/key 형태로 조립
     * - 외부 URL: 그대로 반환
     */
    String getFileUrl(String keyOrUrl);

    /**
     * URL에서 key 추출
     * - 내부 URL: bucket 이후 경로 추출
     * - 외부 URL: 그대로 반환
     */
    String extractKeyFromUrl(String url);

    // ==================== 방식 1: Presigned URL ====================
    // 용도: 에디터 이미지, 대용량 파일
    // 흐름: generatePreSignedUrl() → 프론트에서 temp에 업로드 → moveFile()로 최종 위치 이동

    /**
     * Presigned URL 생성 (temp 폴더에 업로드용)
     */
    PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String dirName);

    /**
     * temp 파일을 최종 위치로 이동
     * @return 이동된 파일의 key
     */
    String moveFile(String sourceKey, String targetDirectory);

    // ==================== 방식 2: 직접 업로드 ====================
    // 용도: 프로필 이미지, 썸네일 등 소용량 파일
    // 흐름: uploadFile() → 바로 최종 위치에 저장

    /**
     * 파일 직접 업로드 (temp 거치지 않고 바로 최종 위치)
     * @return 업로드된 파일의 key
     */
    String uploadFile(MultipartFile file, String directory, String fileName);
}