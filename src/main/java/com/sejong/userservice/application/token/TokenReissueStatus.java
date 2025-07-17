package com.sejong.userservice.application.token;

import org.springframework.http.HttpStatus;

public enum TokenReissueStatus {
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED), // 쿠키에 토큰 없음
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED), // 토큰 자체 만료
    INVALID_OR_REVOKED_TOKEN(HttpStatus.UNAUTHORIZED), // 유효하지 않거나 무효화된 토큰
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED), // 토큰에 해당하는 사용자 없음
    ACCESS_TOKEN_NOT_EXPIRED(HttpStatus.BAD_REQUEST),  // 토큰이 아직 만료되지 않았음
    TOKEN_MISMATCH(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;

    TokenReissueStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
