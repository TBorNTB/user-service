package com.sejong.userservice.application.token;

import com.sejong.userservice.application.common.security.jwt.JWTUtil;
import com.sejong.userservice.application.token.dto.TokenReissueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token", description = "JWT 토큰 관련 API")
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @Operation(summary = "토큰 재발급", description = "만료된 액세스 토큰을 리프레시 토큰을 이용하여 재발급합니다 (회원/관리자 권한 필요)")
    @PostMapping("/reissue")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        String oldAccessToken = jwtUtil.getAccessTokenFromHeader(request);

        TokenReissueResponse reissueResponse = tokenService.reissueTokens(oldAccessToken, oldRefreshToken);

        response.addHeader("Authorization", "Bearer " + reissueResponse.getNewAccessToken());
        response.addCookie(reissueResponse.getNewRefreshTokenCookie());

        return new ResponseEntity<>("토큰이 성공적으로 재발급되었습니다.", HttpStatus.OK);
    }
}
