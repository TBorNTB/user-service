package com.sejong.userservice.client.file;

import com.sejong.userservice.client.file.dto.PreSignedUrl;
import com.sejong.userservice.support.common.util.Filepath;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 인터페이스
 *
 * 두 가지 업로드 방식 지원:
 * 1. Presigned URL 방식 - 대용량 파일, 에디터 이미지 등 (프론트에서 직접 S3 업로드)
 * 2. 직접 업로드 방식 - 프로필 이미지 등 소용량 파일 (백엔드 경유)
 */
public interface FileUploader {

    // 공통

    // 파일 삭제
    void delete(Filepath filepath);

    // key로 파일 URL 조회
    Filepath getFileUrl(String key);

    // 방식 1: Presigned URL (프론트 직접 업로드)
    // 사용 시나리오: 에디터 이미지, 대용량 파일
    // 흐름: generatePreSignedUrl() → 프론트에서 temp에 업로드 → 저장 시 moveFile()로 최종 위치 이동

    // Presigned URL 생성 (temp 폴더에 업로드용)
    PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String dirName);

    // temp 파일을 최종 위치로 이동
    Filepath moveFile(String sourceKey, String targetDirectory);

    // 방식 2: 직접 업로드 (백엔드 경유)
    // 사용 시나리오: 프로필 이미지, 썸네일 등 소용량 파일
    // 흐름: uploadFile() → 바로 최종 위치에 저장

    // 파일 직접 업로드 (temp 거치지 않고 바로 최종 위치)
    Filepath uploadFile(MultipartFile file, String directory, String fileName);
}