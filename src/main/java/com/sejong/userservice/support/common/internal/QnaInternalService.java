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

@Slf4j
@Component
@RequiredArgsConstructor
public class QnaInternalService {

    private final ProjectClient projectClient;

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "validateQuestionExistsFallback")
    public String validateQuestionExists(Long questionId) {
        ResponseEntity<PostLikeCheckResponse> response = projectClient.checkQnaQuestion(questionId);
        log.info("response: {}", response.getBody());
        if (response.getBody() == null || !response.getBody().isStored()) {
            log.info("QnA Question 검증 실패");
            throw new BaseException(BAD_REQUEST);
        }
        return response.getBody().getOwnerUsername();
    }

    private String validateQuestionExistsFallback(Long questionId, Throwable t) {
        if (t instanceof BaseException) {
            throw (BaseException) t;
        }
        throw new BaseException(EXTERNAL_SERVER_ERROR);
    }

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "validateAnswerExistsFallback")
    public String validateAnswerExists(Long answerId) {
        ResponseEntity<PostLikeCheckResponse> response = projectClient.checkQnaAnswer(answerId);
        log.info("response: {}", response.getBody());
        if (response.getBody() == null || !response.getBody().isStored()) {
            log.info("QnA Answer 검증 실패");
            throw new BaseException(BAD_REQUEST);
        }
        return response.getBody().getOwnerUsername();
    }

    private String validateAnswerExistsFallback(Long answerId, Throwable t) {
        if (t instanceof BaseException) {
            throw (BaseException) t;
        }
        throw new BaseException(EXTERNAL_SERVER_ERROR);
    }
}
