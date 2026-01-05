package com.sejong.userservice.support.common.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Long getCount(String key) {
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0;
    }

    public void setCount(String key, Long count) {
        redisTemplate.opsForValue().set(key, String.valueOf(count));
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Boolean setIfAbsent(String key, Long count) {
        return redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(count));
    }

    public Boolean setIfAbsent(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }
}
