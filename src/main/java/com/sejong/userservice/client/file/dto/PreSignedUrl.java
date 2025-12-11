package com.sejong.userservice.client.file.dto;

public record PreSignedUrl(
        String uploadUrl,
        String key,
        String downloadUrl,
        long expirationTime
) {
}
