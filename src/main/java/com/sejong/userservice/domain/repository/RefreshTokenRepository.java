package com.sejong.userservice.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository {
    void saveRefreshToken(String token, String username, LocalDateTime expiryDate, String jti);

    Optional<String> findUsernameByToken(String token);

    Optional<String> findUsernameByJti(String jti);

    boolean revokeToken(String token);

    boolean revokeTokenByJti(String jti);

    boolean isTokenValidOnServer(String token);

    void revokeAllTokensForUser(String username);
}