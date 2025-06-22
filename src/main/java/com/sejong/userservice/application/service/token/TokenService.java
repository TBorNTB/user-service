package com.sejong.userservice.application.service.token;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import com.sejong.userservice.domain.repository.UserRepository;
import com.sejong.userservice.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 액세스 토큰과 리프레시 토큰을 재발급합니다. accessToken, refreshToken을 모두 재발급합니다.(RefreshToken Rotation)
     * 리프레시 토큰 유효성 검사, 무효화, 새로운 토큰 생성 로직을 처리합니다.
     *
     * @param oldRefreshToken 클라이언트가 제공한 이전 리프레시 토큰
     * @return 새로운 액세스 토큰과 새로운 리프레시 토큰 쿠키를 포함하는 TokenReissueResponse 객체
     * @throws TokenReissueException 토큰 재발급 중 문제가 발생할 경우 발생합니다.
     */
    @Transactional // 토큰 관련 작업의 원자성 보장 (예: 이전 토큰 무효화 및 새 토큰 저장)
    public TokenReissueResponse reissueTokens(String oldRefreshToken) {
        // 1. 기본 유효성 검사: 리프레시 토큰 존재 여부 확인
        if (oldRefreshToken == null) {
            throw new TokenReissueException("쿠키에 리프레시 토큰이 없습니다.", TokenReissueStatus.TOKEN_NOT_FOUND);
        }

        // 2. JWT 자체의 만료 여부 확인 (서명 검증 포함)
        if (jwtUtil.isExpired(oldRefreshToken)) {
            String oldJti = jwtUtil.getJti(oldRefreshToken);
            if (oldJti != null) {
                // 만료된 토큰이라도 JTI를 사용하여 DB에서 즉시 무효화 (재사용 방지)
                refreshTokenRepository.revokeTokenByJti(oldJti);
            }
            throw new TokenReissueException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.", TokenReissueStatus.EXPIRED_TOKEN);
        }

        // 3. 사용자 이름(username)과 JTI를 추출하고, 서버에 저장된 유효한 토큰인지 검증
        String username = jwtUtil.getUsername(oldRefreshToken);
        String oldJti = jwtUtil.getJti(oldRefreshToken);

        if (username == null || oldJti == null || !refreshTokenRepository.isTokenValidOnServer(oldRefreshToken)) {
            // **재사용 공격(Replay Attack) 감지:** 유효하지 않거나 이미 무효화된 토큰으로 재발급을 시도하면,
            // 해당 사용자의 모든 리프레시 토큰을 강제로 무효화하여 모든 세션을 종료합니다.
            if (username != null) {
                refreshTokenRepository.revokeAllTokensForUser(username);
            }
            throw new TokenReissueException("유효하지 않거나 무효화된 리프레시 토큰입니다. 다시 로그인해주세요.", TokenReissueStatus.INVALID_OR_REVOKED_TOKEN);
        }

        // 4. 데이터베이스에서 사용자 정보 (역할) 가져오기
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // 사용자 정보가 없으면 해당 토큰도 무효화
            refreshTokenRepository.revokeTokenByJti(oldJti);
            throw new TokenReissueException("제공된 리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다.", TokenReissueStatus.USER_NOT_FOUND);
        }

        // 5. 이전 리프레시 토큰을 서버에서 무효화
        refreshTokenRepository.revokeTokenByJti(oldJti);

        // 6. 새로운 액세스 토큰과 새로운 리프레시 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(username, user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(username); // 새로운 JTI 포함

        // 7. 새로운 리프레시 토큰을 DB에 저장
        String newJti = jwtUtil.getJti(newRefreshToken);
        LocalDateTime newExpiryDate = jwtUtil.getExpirationLocalDateTime(newRefreshToken);
        refreshTokenRepository.saveRefreshToken(newRefreshToken, username, newExpiryDate, newJti);

        // 8. 새로운 리프레시 토큰을 HTTP Only 쿠키로 생성
        Cookie newRefreshTokenCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

        // 재발급된 토큰 정보를 담아 반환
        return new TokenReissueResponse(newAccessToken, newRefreshTokenCookie);
    }
}