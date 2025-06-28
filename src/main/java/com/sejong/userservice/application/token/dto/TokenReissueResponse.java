package com.sejong.userservice.application.token.dto;

import lombok.Getter;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenReissueResponse {
    private final String newAccessToken;
    private final Cookie newRefreshTokenCookie;
}