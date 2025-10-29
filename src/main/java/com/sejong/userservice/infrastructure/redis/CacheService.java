package com.sejong.userservice.infrastructure.redis;

import com.sejong.userservice.application.user.dto.VerificationRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Duration TTL = Duration.ofMinutes(10);
    private final RedisTemplate<String, Object> redisTemplate;

    private String key(String email) {
        return "verify:" + email;
    }

    public void save(VerificationRequest request) {
        String email = request.getEmail();
        String randomCode = request.getRandomCode();
        redisTemplate.opsForValue().set(key(email), randomCode, TTL);
    }
}
