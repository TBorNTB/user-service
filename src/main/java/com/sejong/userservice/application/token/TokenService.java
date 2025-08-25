package com.sejong.userservice.application.token;

import static com.sejong.userservice.application.common.exception.ExceptionType.EXPIRED_TOKEN;
import static com.sejong.userservice.application.common.exception.ExceptionType.INVALID_OR_REVOKED_TOKEN;
import static com.sejong.userservice.application.common.exception.ExceptionType.TOKEN_MISMATCH;

import com.sejong.userservice.application.common.exception.BaseException;
import com.sejong.userservice.application.common.security.jwt.JWTUtil;
import com.sejong.userservice.application.token.dto.TokenReissueResponse;
import com.sejong.userservice.core.token.TokenBlacklistRepository;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TokenService {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenService(JWTUtil jwtUtil, UserRepository userRepository, TokenBlacklistRepository tokenBlacklistRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Transactional
    public TokenReissueResponse reissueTokens(String oldAccessToken, String oldRefreshToken) {
        // 1. 기본 검증
        validateTokensForReissue(oldAccessToken, oldRefreshToken);
        
        // 2. 사용자 정보 조회
        String username = jwtUtil.getUsername(oldRefreshToken);
        User user = userRepository.findByUsername(username);
        
        // 3. 기존 토큰 무효화
        blacklistOldTokens(oldAccessToken, oldRefreshToken);
        
        // 4. 새 토큰 발급
        return issueNewTokens(username, user.getRole().name());
    }

    private void validateTokensForReissue(String oldAccessToken, String oldRefreshToken) {
        validateTokenUserMatch(oldAccessToken, oldRefreshToken);
        
        // 리프레시 토큰 만료 확인
        if (jwtUtil.isExpired(oldRefreshToken)) {
            log.warn("재발급 시도 실패: 리프레시 토큰이 만료되었습니다.");
            throw new BaseException(EXPIRED_TOKEN);
        }

        // 리프레시 토큰 블랙리스트 확인
        String refreshTokenJti = jwtUtil.getJti(oldRefreshToken);
        if (tokenBlacklistRepository.isBlacklisted(refreshTokenJti)) {
            log.error("재발급 시도 실패: 블랙리스트에 등록된 리프레시 토큰입니다.");
            throw new BaseException(INVALID_OR_REVOKED_TOKEN);
        }
    }

    private void validateTokenUserMatch(String oldAccessToken, String oldRefreshToken) {
        if (!jwtUtil.isSameUser(oldAccessToken, oldRefreshToken)) {
            log.error("재발급 시도 실패: 토큰 사용자 불일치");
            throw new BaseException(TOKEN_MISMATCH);
        }
    }

    private void blacklistOldTokens(String oldAccessToken, String oldRefreshToken) {
        String oldAccessTokenJti = jwtUtil.getJti(oldAccessToken);
        String oldRefreshTokenJti = jwtUtil.getJti(oldRefreshToken);
        
        addJtiToBlacklist(oldAccessTokenJti, oldAccessToken);
        addJtiToBlacklist(oldRefreshTokenJti, oldRefreshToken);
        
        log.info("기존 토큰들 블랙리스트에 추가");
    }

    private TokenReissueResponse issueNewTokens(String username, String role) {
        String newAccessToken = jwtUtil.createAccessToken(username, role);
        String newRefreshToken = jwtUtil.createRefreshToken(username);
        Cookie newRefreshTokenCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

        log.info("새 토큰 발급 완료. User: {}", username);
        return new TokenReissueResponse(newAccessToken, newRefreshTokenCookie);
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !jwtUtil.isExpired(accessToken)) {
            String accessTokenJti = jwtUtil.getJti(accessToken);
            addJtiToBlacklist(accessTokenJti, accessToken);
            log.info("액세스 토큰 JTI를 블랙리스트에 추가했습니다. JTI: {}", accessTokenJti);
        }

        if (refreshToken != null && !jwtUtil.isExpired(refreshToken)) {
            String refreshTokenJti = jwtUtil.getJti(refreshToken);
            addJtiToBlacklist(refreshTokenJti, refreshToken);
            log.info("리프레시 토큰 JTI를 블랙리스트에 추가했습니다. JTI: {}", refreshTokenJti);
        }
    }

    private void addJtiToBlacklist(String jti, String token) {
        if (jti == null || token == null || jwtUtil.isExpired(token)) {
            return;
        }

        LocalDateTime expirationTime = jwtUtil.getExpirationLocalDateTime(token);
        Duration ttl = Duration.between(LocalDateTime.now(), expirationTime);
        
        if (ttl.isPositive()) {
            tokenBlacklistRepository.addToBlacklist(jti, ttl);
        }
    }
}