package com.sejong.userservice.api.controller;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import com.sejong.userservice.domain.repository.UserRepository;
import com.sejong.userservice.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class TokenController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenController(JWTUtil jwtUtil, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. HTTP Only 쿠키에서 리프레시 토큰 추출 (클라이언트가 보낸 '이전' 리프레시 토큰)
        String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        if (oldRefreshToken == null) {
            return new ResponseEntity<>("Refresh token not found in cookie.", HttpStatus.UNAUTHORIZED);
        }

        // 2. JWT 자체의 만료 여부 확인 (서명 검증 포함)
        if (jwtUtil.isExpired(oldRefreshToken)) {
            // JWT 자체는 만료되었으나, 서버 DB에 유효한지 확인하여 재사용 공격 방지
            // 만료된 JWT라도 JTI를 추출하여 DB에서 무효화해야 합니다.
            String oldJti = jwtUtil.getJti(oldRefreshToken); // 만료된 토큰에서도 JTI는 추출될 수 있음
            if (oldJti != null) {
                refreshTokenRepository.revokeTokenByJti(oldJti); // 서버 DB에서 즉시 무효화
            }
            return new ResponseEntity<>("Refresh token expired. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        // 3. 서버 DB에 저장된 리프레시 토큰인지, 그리고 무효화되지 않았는지 확인
        // 이 과정에서 JWTUtil.getUsername(oldRefreshToken)이 먼저 호출되어 유효성 검증을 다시 수행하게 됩니다.
        // 클라이언트가 보낸 oldRefreshToken이 DB에 유효하게 저장된 토큰인지 확인합니다.
        String username = jwtUtil.getUsername(oldRefreshToken); // 토큰에서 사용자 이름 추출 (여기서 JWT 유효성 재확인)
        String oldJti = jwtUtil.getJti(oldRefreshToken);

        if (username == null || oldJti == null || !refreshTokenRepository.isTokenValidOnServer(oldRefreshToken)) {
            // 중요: 만약 유효하지 않은(예: 존재하지 않거나, 이미 무효화된) Refresh Token으로 재발급을 시도하면,
            // 해당 사용자의 모든 리프레시 토큰을 강제로 무효화하여 모든 세션을 만료시킵니다.
            // 이는 Replay Attack 시도를 감지하고 차단하는 중요한 단계입니다.
            if (username != null) { // 유저네임이라도 추출됐다면 해당 유저의 모든 토큰 무효화
                refreshTokenRepository.revokeAllTokensForUser(username);
            }
            return new ResponseEntity<>("Invalid or revoked refresh token. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        // 4. DB에서 사용자 정보 (역할) 가져오기
        User user = userRepository.findByUsername(username);
        if (user == null) {
            refreshTokenRepository.revokeTokenByJti(oldJti); // 유저가 없으면 해당 토큰 무효화
            return new ResponseEntity<>("User not found for provided refresh token.", HttpStatus.UNAUTHORIZED);
        }

        String role = user.getRole();

        // 5. 이전 Refresh Token을 서버에서 무효화 (RTR의 핵심)
        refreshTokenRepository.revokeTokenByJti(oldJti); // 사용된 이전 Refresh Token 무효화

        // 6. 새로운 액세스 토큰과 새로운 리프레시 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(username, role);
        String newRefreshToken = jwtUtil.createRefreshToken(username); // 새로운 JTI 포함

        // 7. 새로운 리프레시 토큰을 DB에 저장
        String newJti = jwtUtil.getJti(newRefreshToken);
        LocalDateTime newExpiryDate = jwtUtil.getExpirationLocalDateTime(newRefreshToken);
        refreshTokenRepository.saveRefreshToken(newRefreshToken, username, newExpiryDate, newJti);

        // 8. 응답 헤더에 새 액세스 토큰 추가
        response.addHeader("Authorization", "Bearer " + newAccessToken);

        // 9. 새로운 리프레시 토큰을 HTTP Only 쿠키로 클라이언트에 전달
        Cookie newRefreshTokenCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);
        response.addCookie(newRefreshTokenCookie);

        return new ResponseEntity<>("Tokens reissued successfully.", HttpStatus.OK);
    }
}
