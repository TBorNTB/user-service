package com.sejong.userservice.domain.token;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.INVALID_OR_REVOKED_TOKEN;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.domain.token.dto.TokenReissueResponse;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Transactional
    public TokenReissueResponse reissueTokens(String accessToken, String refreshToken) {
        validateTokensForReissue(accessToken, refreshToken);

        String username = jwtUtil.getUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        blacklistOldTokens(accessToken, refreshToken);

        return issueNewTokens(username, user.getRole().name());
    }

    private void validateTokensForReissue(String accessToken, String refreshToken) {
        jwtUtil.validateSameUser(accessToken, refreshToken);
        jwtUtil.validateToken(refreshToken);

        String refreshTokenJti = jwtUtil.getJti(refreshToken);
        if (tokenBlacklistRepository.isBlacklisted(refreshTokenJti)) {
            log.error("재발급 시도 실패: 블랙리스트에 등록된 리프레시 토큰입니다.");
            throw new BaseException(INVALID_OR_REVOKED_TOKEN);
        }
    }

    private void blacklistOldTokens(String oldAccessToken, String oldRefreshToken) {
        blacklist(oldAccessToken, oldRefreshToken);
        log.info("기존 토큰들 블랙리스트에 추가");
    }

    private TokenReissueResponse issueNewTokens(String username, String role) {
        String newAccessToken = jwtUtil.createAccessToken(username, role);
        String newRefreshToken = jwtUtil.createRefreshToken(username);
        Cookie newAccessTokenCookie = jwtUtil.createAccessTokenCookie(newAccessToken);
        Cookie newRefreshTokenCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

        log.info("새 토큰 발급 완료. User: {}", username);
        return new TokenReissueResponse(newAccessTokenCookie, newRefreshTokenCookie);
    }

    public void blacklist(String accessToken, String refreshToken) {
        String accessJti = jwtUtil.getJti(accessToken);
        Duration accessTTL = jwtUtil.getTTL(accessToken);
        addJtiToBlacklist(accessJti, accessTTL);
        log.info("액세스 토큰 JTI를 블랙리스트에 추가했습니다. JTI: {}", accessJti);

        String refreshTokenJti = jwtUtil.getJti(refreshToken);
        Duration refreshTTL = jwtUtil.getTTL(refreshToken);
        addJtiToBlacklist(refreshTokenJti, refreshTTL);
        log.info("리프레시 토큰 JTI를 블랙리스트에 추가했습니다. JTI: {}", refreshTokenJti);
    }

    private void addJtiToBlacklist(String jti, Duration ttl) {
        if (ttl.isPositive()) {
            tokenBlacklistRepository.addToBlacklist(jti, ttl);
        }
    }
}