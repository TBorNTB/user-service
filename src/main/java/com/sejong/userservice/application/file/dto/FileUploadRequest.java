package com.sejong.userservice.application.file.dto;

public record FileUploadRequest(
        String fileName,
        String contentType,
        String fileType
) {
}
