package com.sejong.userservice.application.token;

import com.sejong.userservice.application.exception.TokenReissueException;
import com.sejong.userservice.application.token.dto.TokenReissueResponse;
import com.sejong.userservice.infrastructure.common.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN')")
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
