package com.sejong.userservice.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository {
    // 새로운 리프레시 토큰 저장
    void saveRefreshToken(String token, String username, LocalDateTime expiryDate, String jti);

    // 토큰 문자열로 리프레시 토큰 엔티티 조회 (유효성 검사는 포함하지 않음)
    Optional<String> findUsernameByToken(String token);

    // JTI로 리프레시 토큰 엔티티 조회
    Optional<String> findUsernameByJti(String jti);

    // 특정 토큰을 무효화 (revoked 상태로 변경)
    boolean revokeToken(String token);

    // JTI로 특정 토큰을 무효화
    boolean revokeTokenByJti(String jti);

    // 특정 토큰이 서버에 유효한지 확인 (만료되지 않고, 무효화되지 않음)
    boolean isTokenValidOnServer(String token);

    // 특정 사용자의 모든 리프레시 토큰 무효화 (로그아웃 등)
    void revokeAllTokensForUser(String username);
}