package com.sejong.userservice.support.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpServletRequest request, BaseException e) {
        ExceptionType type = e.exceptionType();
        log.info("잘못된 요청이 들어왔습니다. URI: {},  내용:  {}", request.getRequestURI(), type.description());
        return ResponseEntity.status(type.httpStatus())
                .body(new ExceptionResponse(type.description()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        log.warn("권한이 없는 요청입니다. URI: {}, 메시지: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse("접근 권한이 없습니다. 관리자에게 문의하세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllUncaughtException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>("Internal Server Error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
