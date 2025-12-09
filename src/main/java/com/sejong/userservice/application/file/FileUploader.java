package com.sejong.userservice.application.file;

import com.sejong.userservice.application.file.dto.PreSignedUrl;
import com.sejong.userservice.common.Filepath;

public interface FileUploader {
    PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String dirName);

    void delete(Filepath filepath);

    Filepath getFileUrl(String key);
}