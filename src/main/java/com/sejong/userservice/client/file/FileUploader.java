package com.sejong.userservice.client.file;

import com.sejong.userservice.client.file.dto.PreSignedUrl;
import com.sejong.userservice.support.common.util.Filepath;

public interface FileUploader {
    PreSignedUrl generatePreSignedUrl(String fileName, String contentType, String dirName);

    void delete(Filepath filepath);

    Filepath getFileUrl(String key);
}