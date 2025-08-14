package com.sejong.userservice.application.common.exception;

import com.sejong.userservice.application.token.TokenReissueStatus;
import lombok.Getter;

@Getter
public class TokenReissueException extends RuntimeException {
    private final TokenReissueStatus status;

    public TokenReissueException(String message, TokenReissueStatus status) {
        super(message);
        this.status = status;
    }
}