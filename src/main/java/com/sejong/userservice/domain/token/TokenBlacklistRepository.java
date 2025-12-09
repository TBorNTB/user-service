package com.sejong.userservice.domain.token;

import java.time.Duration;

public interface TokenBlacklistRepository {
    void addToBlacklist(String jti, Duration ttl);
    boolean isBlacklisted(String jti);
}