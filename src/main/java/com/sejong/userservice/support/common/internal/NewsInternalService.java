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
public class NewsInternalService {

    private final ProjectClient projectClient;

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "validateExistsFallback")
    public String validateExists(Long postId) {
        ResponseEntity<PostLikeCheckResponse> response = projectClient.checkNews(postId);
        log.info("response: {}",response.getBody());
        if (!response.getBody().isStored()) {
            log.info("Archive 검증 실패");
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

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getNewsCountFallback")
    public Long getNewsCount() {
        ResponseEntity<Long> response = projectClient.getNewsCount();
        return response.getBody();
    }

    private Long getNewsCountFallback(Throwable t) {
        // 예외 대신 기본값 0 반환
        return 0L;
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getUserNewsIdsFallback")
    public List<Long> getUserNewsIds(String username) {
        ResponseEntity<List<Long>> response = projectClient.getUserNewsIds(username);
        return response.getBody() != null ? response.getBody() : List.of();
    }

    private List<Long> getUserNewsIdsFallback(String username, Throwable t) {
        return List.of();
    }
}
