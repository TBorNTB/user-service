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

@Slf4j
@Component
@RequiredArgsConstructor
public class CSKnowledgeInternalService {

    private final ProjectClient projectClient;

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "validateExistsFallback")
    public String validateExists(Long postId) {
        ResponseEntity<PostLikeCheckResponse> response = projectClient.checkCSKnowledge(postId);
        log.info("response: {}",response.getBody());
        if (!response.getBody().isStored()) {
            log.info("Article 검증 실패");
            throw new BaseException(BAD_REQUEST);
        }
        return response.getBody().getOwnerUsername();
    }

    private void validateExistsFallback(Long postId, Throwable t) {
        if (t instanceof BaseException) {
            throw (BaseException) t;
        }
        throw new BaseException(EXTERNAL_SERVER_ERROR);
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getCsCountFallback")
    public Long getCsCount() {
        ResponseEntity<Long> response = projectClient.getCsCount();
        return response.getBody();
    }

    private Long getCsCountFallback(Throwable t) {
        // 예외 대신 기본값 0 반환
        return 0L;
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getUserCsKnowledgeIdsFallback")
    public List<Long> getUserCsKnowledgeIds(String username) {
        ResponseEntity<List<Long>> response = projectClient.getUserCsKnowledgeIds(username);
        return response.getBody() != null ? response.getBody() : List.of();
    }

    private List<Long> getUserCsKnowledgeIdsFallback(String username, Throwable t) {
        return List.of();
    }
}
