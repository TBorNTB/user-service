package com.sejong.userservice.client.file;

import com.sejong.userservice.client.file.dto.PreSignedUrl;
import com.sejong.userservice.support.common.util.Filepath;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {

    // 공통
    void delete(Filepath filepath);


    PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String dirName);

    Filepath getFileUrl(String key);

    Filepath moveFile(String sourceKey, String targetDirectory);

    // 직접 업로드
    Filepath uploadFile(MultipartFile file, String directory, String fileName);
}