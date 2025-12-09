package com.sejong.userservice.common.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseException extends RuntimeException {

    private final ExceptionType exceptionType;

    public ExceptionType exceptionType() {
        return exceptionType;
    }
}
