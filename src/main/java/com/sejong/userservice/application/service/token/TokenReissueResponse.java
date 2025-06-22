package com.sejong.userservice.application.service.token;

import lombok.Getter;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenReissueResponse {
    private final String newAccessToken;
    private final Cookie newRefreshTokenCookie;
}