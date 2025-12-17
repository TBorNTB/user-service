package com.sejong.userservice.support.common.exception.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseException extends RuntimeException {

    private final ExceptionType exceptionType;

    public ExceptionType exceptionType() {
        return exceptionType;
    }
}
