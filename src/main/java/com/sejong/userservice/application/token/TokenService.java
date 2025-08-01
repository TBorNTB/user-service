package com.sejong.userservice.application.token;

import com.sejong.userservice.application.exception.TokenReissueException;
import com.sejong.userservice.application.token.dto.TokenReissueResponse;
import com.sejong.userservice.core.token.TokenRepository;
import com.sejong.userservice.core.token.TokenType;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.infrastructure.common.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j // 로그 사용을 위해 추가
@Service
public class TokenService {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    public TokenService(JWTUtil jwtUtil, UserRepository userRepository, TokenRepository tokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public TokenReissueResponse reissueTokens(String oldAccessToken, String oldRefreshToken) {
        if (!jwtUtil.isExpired(oldAccessToken)) {
            log.warn("재발급 시도: 액세스 토큰이 아직 유효합니다. oldAccessToken JTI: {}", jwtUtil.getJti(oldAccessToken));
            throw new TokenReissueException("액세스 토큰이 아직 유효합니다. 리프레시 토큰을 사용하여 재발급해주세요.",
                    TokenReissueStatus.ACCESS_TOKEN_NOT_EXPIRED);
        }

        if (oldRefreshToken == null) {
            log.error("재발급 시도 실패: 쿠키에 리프레시 토큰이 없습니다.");
            throw new TokenReissueException("쿠키에 리프레시 토큰이 없습니다.", TokenReissueStatus.TOKEN_NOT_FOUND);
        }

        String accessTokenUsername = jwtUtil.getUsername(oldAccessToken);
        String refreshTokenUsername = jwtUtil.getUsername(oldRefreshToken);

        if (accessTokenUsername == null || refreshTokenUsername == null || !accessTokenUsername.equals(
                refreshTokenUsername)) {
            log.error("재발급 시도 실패: 액세스 토큰과 리프레시 토큰이 다른 사용자를 가리킵니다. Access Token User: {}, Refresh Token User: {}",
                    accessTokenUsername, refreshTokenUsername);

            throw new TokenReissueException("액세스 토큰과 리프레시 토큰이 일치하지 않습니다.",
                    TokenReissueStatus.TOKEN_MISMATCH);
        }

        if (jwtUtil.isExpired(oldRefreshToken)) {
            String oldJti = jwtUtil.getJti(oldRefreshToken);
            if (oldJti != null) {
                tokenRepository.revokeTokenByJti(oldJti);
                log.info("만료된 리프레시 토큰 JTI 무효화: {}", oldJti);
            }
            log.warn("재발급 시도 실패: 리프레시 토큰이 만료되었습니다. Refresh Token JTI: {}", oldJti);
            throw new TokenReissueException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.", TokenReissueStatus.EXPIRED_TOKEN);
        }

        String username = jwtUtil.getUsername(oldRefreshToken);
        String oldRefreshTokenJti = jwtUtil.getJti(oldRefreshToken);

        if (username == null || oldRefreshTokenJti == null || !tokenRepository.isTokenValidOnServer(oldRefreshToken)) {
            log.error("재사용 공격/유효하지 않은 토큰 감지: 유효하지 않거나 무효화된 리프레시 토큰으로 재발급 시도. User: {}, Old Refresh Token JTI: {}",
                    username, oldRefreshTokenJti);
            if (username != null) {
                tokenRepository.revokeAllTokensForUser(username);
                log.info("재사용 공격 방어: 사용자 {}의 모든 리프레시 토큰을 무효화했습니다.", username);
            }
            throw new TokenReissueException("유효하지 않거나 무효화된 리프레시 토큰입니다. 다시 로그인해주세요.",
                    TokenReissueStatus.INVALID_OR_REVOKED_TOKEN);
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            tokenRepository.revokeTokenByJti(oldRefreshTokenJti); // 리프레시 토큰 JTI 무효화
            log.error("재발급 시도 실패: 제공된 리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다. User: {}", username);
            throw new TokenReissueException("제공된 리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다.", TokenReissueStatus.USER_NOT_FOUND);
        }

        tokenRepository.revokeAllTokensForUser(username);
        log.info("새 토큰 발행 전: 사용자 {}의 기존 모든 토큰을 무효화했습니다.", username);

        String newAccessToken = jwtUtil.createAccessToken(username, user.getRole().name());
        String newRefreshToken = jwtUtil.createRefreshToken(username);

        String newRefreshTokenJti = jwtUtil.getJti(newRefreshToken);
        LocalDateTime newRefreshTokenExpiryDate = jwtUtil.getExpirationLocalDateTime(newRefreshToken);
        tokenRepository.saveToken(newRefreshToken, username, newRefreshTokenExpiryDate, newRefreshTokenJti,
                TokenType.REFRESH);
        log.info("새 리프레시 토큰 서버에 저장됨. JTI: {}", newRefreshTokenJti);

        String newAccessTokenJti = jwtUtil.getJti(newAccessToken);
        LocalDateTime newAccessTokenExpiryDate = jwtUtil.getExpirationLocalDateTime(newAccessToken);
        tokenRepository.saveToken(newAccessToken, username, newAccessTokenExpiryDate, newAccessTokenJti,
                TokenType.ACCESS);
        log.info("새 액세스 토큰 서버에 저장됨. JTI: {}", newAccessTokenJti);

        Cookie newRefreshTokenCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

        return new TokenReissueResponse(newAccessToken, newRefreshTokenCookie);
    }
}