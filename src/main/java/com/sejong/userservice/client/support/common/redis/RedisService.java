package com.sejong.userservice.client.support.common.redis;

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

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    public void markAsViewed(String ipKey, Duration ttl) {
        redisTemplate.opsForValue().set(ipKey, "viewed", ttl);
    }

    public Long getCount(String key) {
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, "0");
        }
        String count = redisTemplate.opsForValue().get(key);
        return Long.parseLong(count);
    }

    public void setCount(String key, Long count) {
        redisTemplate.opsForValue().set(key, String.valueOf(count));
    }

    public Long increment(String key) {
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, "0");
        }
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decrement(String key) {
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, "0");
        }
        return redisTemplate.opsForValue().decrement(key);
    }

    public void clearAllLikeKeys(String pattern) {
        try (org.springframework.data.redis.core.Cursor<String> cursor = redisTemplate.scan(
                org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build())) {
                    
            while (cursor.hasNext()) {
                redisTemplate.delete(cursor.next());
            }
        } catch (Exception e) {
            throw new RuntimeException("키 삭제 실패", e);
        }
    }
}
