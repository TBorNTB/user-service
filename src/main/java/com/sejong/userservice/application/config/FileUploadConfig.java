package com.sejong.userservice.application.config;

import com.sejong.userservice.application.file.FileUploader;
import com.sejong.userservice.infrastructure.file.S3FileUploader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUploadConfig {

    @Bean
    @ConditionalOnProperty(name = "file.upload.type", havingValue = "s3", matchIfMissing = true)
    public FileUploader s3FileUploader(S3FileUploader s3FileUploader) {
        return s3FileUploader;
    }

    @Bean
    @ConditionalOnProperty(name = "file.upload.type", havingValue = "local")
    public FileUploader localFileUploader() {
        // TODO: LocalFileUploader 구현 후 추가하기
        throw new UnsupportedOperationException("LocalFileUploader not implemented yet");
    }
}