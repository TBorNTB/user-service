package com.sejong.userservice.client.support.error;

import com.sejong.metaservice.support.common.exception.BaseException;
import com.sejong.metaservice.support.common.exception.ExceptionType;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Order(value=Integer.MIN_VALUE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity<Object> handleBaseException(HttpServletRequest request, BaseException e){
        ExceptionType type = e.exceptionType();
        log.info("잘못된 요청이 들어왔습니다. URI: {},  내용:  {}", request.getRequestURI(), type.description());
        return ResponseEntity.status(type.httpStatus())
                .body(new ExceptionResponse(type.description()));
    }
}
