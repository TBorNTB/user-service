package com.sejong.userservice.domain.user.repository;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.VERIFICATION_CODE_MISMATCH;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.VERIFICATION_CODE_NOT_FOUND;

import com.sejong.userservice.support.common.exception.type.BaseException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCacheService {

    private static final Duration TTL = Duration.ofMinutes(10);
    private final RedisTemplate<String, String> redisTemplate;

    private String key(String email) {
        return "verify:" + email;
    }

    public void save(String email, String code) {
        redisTemplate.opsForValue().set(key(email), code, TTL);
    }

    public void verify(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(key(email));

        if (storedCode == null) {
            throw new BaseException(VERIFICATION_CODE_NOT_FOUND);
        }

        if (!storedCode.equals(code)) {
            throw new BaseException(VERIFICATION_CODE_MISMATCH);
        }
    }
}
