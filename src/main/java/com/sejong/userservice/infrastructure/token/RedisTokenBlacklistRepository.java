package com.sejong.userservice.infrastructure.token;

import com.sejong.userservice.core.token.TokenBlacklistRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisTokenBlacklistRepository implements TokenBlacklistRepository {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addToBlacklist(String jti, Duration ttl) {
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "revoked", ttl);
        log.info("JTI가 블랙리스트에 추가됨. JTI: {}, TTL: {}초", jti, ttl.getSeconds());
    }

    @Override
    public boolean isBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}