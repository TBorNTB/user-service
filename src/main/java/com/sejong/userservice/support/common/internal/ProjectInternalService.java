package com.sejong.userservice.support.common.internal;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.BAD_REQUEST;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.EXTERNAL_SERVER_ERROR;

import com.sejong.userservice.client.ProjectClient;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.internal.response.PostLikeCheckResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectInternalService {

    private final ProjectClient projectClient;

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "validateExistsFallback")
    public String validateExists(Long postId) {
        log.info("시작...");
        ResponseEntity<PostLikeCheckResponse> response = projectClient.checkProject(postId);
        if (!response.getBody().isStored()) {
            log.info("Project 검증 실패");
            throw new BaseException(BAD_REQUEST);
        }
        return response.getBody().getOwnerUsername();
    }

    private String validateExistsFallback(Long postId, Throwable t) {
        if (t instanceof BaseException) {
            throw (BaseException) t;
        }
        throw new BaseException(EXTERNAL_SERVER_ERROR);
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getUserCountFallback")
    public Long getProjectCount() {
        ResponseEntity<Long> response = projectClient.getProjectCount();
        return response.getBody();
    }

    private Long getUserCountFallback(Throwable t) {
        // 예외 대신 기본값 0 반환
        return 0L;
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getCsCountFallback")
    public Long getCsCount() {
        ResponseEntity<Long> response = projectClient.getCsCount();
        return response.getBody();
    }

    private Long getCsCountFallback(Throwable t) {
        return 0L;
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getUserProjectIdsFallback")
    public List<Long> getUserProjectIds(String username) {
        ResponseEntity<List<Long>> response = projectClient.getUserProjectIds(username);
        return response.getBody() != null ? response.getBody() : List.of();
    }

    private List<Long> getUserProjectIdsFallback(String username, Throwable t) {
        return List.of();
    }
}
