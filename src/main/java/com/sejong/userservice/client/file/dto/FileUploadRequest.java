package com.sejong.userservice.client.file.dto;

public record FileUploadRequest(
        String fileName,
        String contentType,
        String fileType
) {
}
