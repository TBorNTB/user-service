package com.sejong.userservice.core.token;

import java.time.LocalDateTime;

public interface TokenRepository {
    String saveToken(String token, String username, LocalDateTime expiryDate, String jti, TokenType tokenType);

    String findUsernameByToken(String token);

    String findUsernameByJti(String jti);

    boolean revokeToken(String token);

    boolean revokeTokenByJti(String jti);

    boolean isTokenValidOnServer(String token);

    void revokeAllTokensForUser(String username);
}