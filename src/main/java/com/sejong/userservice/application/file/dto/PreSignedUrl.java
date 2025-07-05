package com.sejong.userservice.application.file.dto;

public record PreSignedUrl(
        String uploadUrl,
        String key,
        String downloadUrl,
        long expirationTime
) {
}
