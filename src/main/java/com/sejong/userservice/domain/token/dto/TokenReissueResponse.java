package com.sejong.userservice.domain.token.dto;

import jakarta.servlet.http.Cookie;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenReissueResponse {
    private final Cookie newAccessTokenCookie;
    private final Cookie newRefreshTokenCookie;
}