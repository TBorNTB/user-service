package com.sejong.userservice.client.support.common.internal;

import com.sejong.metaservice.client.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserInternalService {

    private final UserClient userClient;

    @CircuitBreaker(name = "myFeignClient", fallbackMethod = "getUserCountFallback")
    public Long getUserCount() {
        ResponseEntity<Long> response = userClient.getUserCount();
        return response.getBody();
    }

    private Long getUserCountFallback(Throwable t) {
        return 0L;
    }
}
