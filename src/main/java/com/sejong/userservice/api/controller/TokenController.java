package com.sejong.userservice.api.controller;

import com.sejong.userservice.application.service.token.TokenReissueException;
import com.sejong.userservice.application.service.token.TokenReissueResponse;
import com.sejong.userservice.application.service.token.TokenService;
import com.sejong.userservice.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. HTTP Only 쿠키에서 리프레시 토큰 추출 (컨트롤러의 역할)
        String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        try {
            // 2. 토큰 재발급 로직을 서비스에 위임
            TokenReissueResponse reissueResponse = tokenService.reissueTokens(oldRefreshToken);

            // 3. 서비스에서 받은 새 액세스 토큰을 응답 헤더에 추가
            response.addHeader("Authorization", "Bearer " + reissueResponse.getNewAccessToken());
            // 4. 서비스에서 받은 새 리프레시 토큰 쿠키를 응답에 추가
            response.addCookie(reissueResponse.getNewRefreshTokenCookie());

            // 5. 성공 응답 반환
            return new ResponseEntity<>("토큰이 성공적으로 재발급되었습니다.", HttpStatus.OK);
        } catch (TokenReissueException e) {
            // 6. 서비스에서 발생한 특정 예외에 따라 HTTP 상태 코드와 메시지 반환
            return new ResponseEntity<>(e.getMessage(), e.getStatus().getHttpStatus());
        }
    }
}
