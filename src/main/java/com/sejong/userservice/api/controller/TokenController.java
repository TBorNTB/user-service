package com.sejong.userservice.api.controller;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import com.sejong.userservice.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public TokenController(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. HTTP Only 쿠키에서 리프레시 토큰 추출
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return new ResponseEntity<>("Refresh token not found.", HttpStatus.UNAUTHORIZED);
        }

        // 2. 리프레시 토큰 유효성 검증 (만료 여부 포함)
        if (jwtUtil.isExpired(refreshToken)) { // isExpired가 true를 반환하면 만료된 것
            // 만료된 리프레시 토큰은 무효화하고 클라이언트에게 다시 로그인하라고 알림
            Cookie expiredCookie = new Cookie("refreshToken", null);
            expiredCookie.setMaxAge(0); // 쿠키 즉시 만료
            expiredCookie.setHttpOnly(true);
            expiredCookie.setPath("/");
            response.addCookie(expiredCookie);
            return new ResponseEntity<>("Refresh token expired. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        // 3. 리프레시 토큰에서 사용자 이름 추출
        String username = jwtUtil.getUsername(refreshToken);

        // 4. DB에서 사용자 정보(특히 역할)를 가져옵니다.
        // 사용자 이름으로 User 객체를 조회하여 역할을 가져와야 합니다.
        User user = userRepository.findByUsername(username);

        if (user == null) {
            // 토큰에는 유저네임이 있지만 DB에 유저가 없는 경우 (예: 삭제된 유저)
            return new ResponseEntity<>("User not found for provided refresh token.", HttpStatus.UNAUTHORIZED);
        }

        String role = user.getRole();

        // 5. 새로운 액세스 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(username, role);

        // 6. 응답 헤더에 새 액세스 토큰 추가
        response.addHeader("Authorization", "Bearer " + newAccessToken);

        return new ResponseEntity<>("Access token reissued successfully.", HttpStatus.OK);
    }
}
