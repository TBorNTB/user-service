package com.sejong.userservice.application.token;

import com.sejong.userservice.application.exception.TokenReissueException;
import com.sejong.userservice.application.token.dto.TokenReissueResponse;
import com.sejong.userservice.core.token.RefreshTokenRepository;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.infrastructure.common.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
public class TokenService {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(JWTUtil jwtUtil, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public TokenReissueResponse reissueTokens(String oldRefreshToken) {
        if (oldRefreshToken == null) {
            throw new TokenReissueException("쿠키에 리프레시 토큰이 없습니다.", TokenReissueStatus.TOKEN_NOT_FOUND);
        }

        if (jwtUtil.isExpired(oldRefreshToken)) {
            String oldJti = jwtUtil.getJti(oldRefreshToken);
            if (oldJti != null) {
                refreshTokenRepository.revokeTokenByJti(oldJti);
            }
            throw new TokenReissueException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.", TokenReissueStatus.EXPIRED_TOKEN);
        }

        String username = jwtUtil.getUsername(oldRefreshToken);
        String oldJti = jwtUtil.getJti(oldRefreshToken);

        if (username == null || oldJti == null || !refreshTokenRepository.isTokenValidOnServer(oldRefreshToken)) {
            // 재사용 공격(Replay Attack) 감지: 유효하지 않거나 이미 무효화된 토큰으로 재발급을 시도하면,
            // 해당 사용자의 모든 리프레시 토큰을 강제로 무효화하여 모든 세션을 종료합니다.
            if (username != null) {
                refreshTokenRepository.revokeAllTokensForUser(username);
            }
            throw new TokenReissueException("유효하지 않거나 무효화된 리프레시 토큰입니다. 다시 로그인해주세요.",
                    TokenReissueStatus.INVALID_OR_REVOKED_TOKEN);
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            refreshTokenRepository.revokeTokenByJti(oldJti);
            throw new TokenReissueException("제공된 리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다.", TokenReissueStatus.USER_NOT_FOUND);
        }

        refreshTokenRepository.revokeTokenByJti(oldJti);

        String newAccessToken = jwtUtil.createAccessToken(username, user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(username); // 새로운 JTI 포함

        String newJti = jwtUtil.getJti(newRefreshToken);
        LocalDateTime newExpiryDate = jwtUtil.getExpirationLocalDateTime(newRefreshToken);
        refreshTokenRepository.saveRefreshToken(newRefreshToken, username, newExpiryDate, newJti);

        Cookie newRefreshTokenCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

        return new TokenReissueResponse(newAccessToken, newRefreshTokenCookie);
    }

    @RestController
    @RequestMapping("/token")
    @RequiredArgsConstructor
    public static class TokenController {

        private final JWTUtil jwtUtil;
        private final TokenService tokenService;

        @PostMapping("/reissue")
        public ResponseEntity<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {
            String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);

            try {
                TokenReissueResponse reissueResponse = tokenService.reissueTokens(oldRefreshToken);

                response.addHeader("Authorization", "Bearer " + reissueResponse.getNewAccessToken());
                response.addCookie(reissueResponse.getNewRefreshTokenCookie());

                return new ResponseEntity<>("토큰이 성공적으로 재발급되었습니다.", HttpStatus.OK);
            } catch (TokenReissueException e) {
                return new ResponseEntity<>(e.getMessage(), e.getStatus().getHttpStatus());
            }
        }
    }
}