package com.sejong.userservice.client.support.common.internal;

import static com.sejong.metaservice.support.common.exception.ExceptionType.BAD_REQUEST;
import static com.sejong.metaservice.support.common.exception.ExceptionType.EXTERNAL_SERVER_ERROR;

import com.sejong.metaservice.client.ArchiveClient;
import com.sejong.metaservice.support.common.exception.BaseException;
import com.sejong.metaservice.support.common.internal.response.PostLikeCheckResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsInternalService {

    private final ArchiveClient archiveClient;

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "validateExistsFallback")
    public String validateExists(Long postId) {
        ResponseEntity<PostLikeCheckResponse> response = archiveClient.checkNews(postId);
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
        ResponseEntity<Long> response = archiveClient.getNewsCount();
        return response.getBody();
    }

    private Long getNewsCountFallback(Throwable t) {
        // 예외 대신 기본값 0 반환
        return 0L;
    }
}
